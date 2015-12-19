/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.internal.Conflicts;
import com.threecrickets.creel.internal.DaemonThreadFactory;
import com.threecrickets.creel.internal.IdentificationContext;
import com.threecrickets.creel.internal.Modules;
import com.threecrickets.creel.util.ConfigHelper;
import com.threecrickets.creel.util.Jobs;

/**
 * Handles identifying and installing modules.
 * 
 * @author Tal Liron
 */
public class Manager extends Notifier
{
	public enum ConflictPolicy
	{
		NEWEST, OLDEST
	};

	//
	// Construction
	//

	public Manager()
	{
		super( new EventHandlers() );
	}

	//
	// Attributes
	//

	/**
	 * Creates module instances based on module specification configs.
	 * <p>
	 * If the platform is not specified in the config, it will be
	 * defaultPlatform.
	 * 
	 * @param moduleSpecificationConfigs
	 */
	public void setExplicitModules( Collection<Map<String, ?>> moduleSpecificationConfigs )
	{
		explicitModules.clear();
		for( Map<String, ?> config : moduleSpecificationConfigs )
		{
			ConfigHelper configHelper = new ConfigHelper( config );
			String type = configHelper.getString( "type", defaultPlatform );
			ModuleSpecification moduleSpecification = ConfigHelper.newModuleSpecification( type, config );
			Module module = new Module( true, null, moduleSpecification );
			explicitModules.add( module );
		}
	}

	/**
	 * Creates repository instances based on configs.
	 * <p>
	 * If the platform is not specified in the config, it will be
	 * defaultPlatform.
	 * 
	 * @param repositoryConfigs
	 */
	public void setRepositories( Collection<Map<String, ?>> repositoryConfigs )
	{
		repositories.clear();
		for( Map<String, ?> config : repositoryConfigs )
		{
			ConfigHelper configHelper = new ConfigHelper( config );
			String type = configHelper.getString( "type", defaultPlatform );
			Repository repository = ConfigHelper.newRepository( type, config );
			repositories.add( repository );
		}
	}

	public String getDefaultPlatform()
	{
		return defaultPlatform;
	}

	public void setDefaultPlatform( String defaultPlatform )
	{
		this.defaultPlatform = defaultPlatform;
	}

	public ConflictPolicy getConflictPolicy()
	{
		return conflictPolicy;
	}

	public void setConflictPolicy( ConflictPolicy conflictPolicy )
	{
		this.conflictPolicy = conflictPolicy;
	}

	public Iterable<Module> getExplicitModules()
	{
		return Collections.unmodifiableCollection( explicitModules );
	}

	public Iterable<Repository> getRepositories()
	{
		return Collections.unmodifiableCollection( repositories );
	}

	public Iterable<Rule> getRules()
	{
		return Collections.unmodifiableCollection( rules );
	}

	public Iterable<Module> getIdentifiedModules()
	{
		return identifiedModules;
	}

	public Iterable<Module> getUnidentifiedModules()
	{
		return unidentifiedModules;
	}

	public Iterable<Conflict> getConflicts()
	{
		return conflicts;
	}

	public ExecutorService getExecutor()
	{
		return executor;
	}

	public int getIdentifiedCacheHits()
	{
		return identifiedCacheHits.get();
	}

	public void addModule( Module module )
	{
		if( module.getIdentifier() != null )
			identifiedModules.addByIdentifier( module );
		else
			unidentifiedModules.addBySpecification( module );
	}

	public void replaceModule( Module oldModule, Module newModule )
	{
		for( ListIterator<Module> i = explicitModules.listIterator(); i.hasNext(); )
		{
			Module explicitModule = i.next();
			if( ( explicitModule.getIdentifier() != null ) && ( explicitModule.getIdentifier().equals( oldModule.getIdentifier() ) ) )
			{
				explicitModule = newModule;
				i.set( explicitModule );
				explicitModule.setExplicit( true );
			}
			explicitModule.replaceModule( oldModule, newModule, true );
		}
	}

	//
	// Operations
	//

	/**
	 * Goes over explicitModules and identifies them recursively. This is done
	 * using fork/join parallelism for better efficiency.
	 * <p>
	 * When finished, identifiedModules and unidentifiedModules would be filled
	 * appropriately.
	 */
	public synchronized void identify()
	{
		String id = begin( "Identifying" );

		identifyModuleJobs.clear();
		Phaser phaser = new Phaser( 1 );
		for( Module explicitModule : getExplicitModules() )
			identifyModuleFuture( explicitModule, true, phaser );
		phaser.arriveAndAwaitAdvance();

		int count = identifiedModules.size();

		resolveConflicts();

		identifiedModules.sortByIdentifiers();
		unidentifiedModules.sortBySpecifications();

		end( id, "Made " + count + " identifications" );
	}

	/**
	 * Identifies a module, optionally identifying its dependencies recursively
	 * (supporting efficient multithreaded parallelism that avoids repeating
	 * work already done).
	 * <p>
	 * "Identification" means finding the best identifier available from all the
	 * candidates in all the repositories that match the specification. A
	 * successful identification results in the the module has an identifier. An
	 * unidentified module has only a specification, but no identifier.
	 * <p>
	 * A cache of identified modules is maintained in the manager to avoid
	 * identifying the same module twice.
	 * 
	 * @param module
	 * @param recursive
	 * @param phaser
	 */
	public void identifyModule( final Module module, final boolean recursive, final Phaser phaser )
	{
		IdentificationContext context = new IdentificationContext( getRepositories(), recursive );

		applyRules( module, context );

		if( context.isExclude() )
		{
			// Mark to not identify this specification
			unidentifiedModules.addBySpecification( module );
		}
		else
		{
			if( module.getIdentifier() != null )
			{
				// Nothing to do: already identified
			}
			else if( unidentifiedModules.get( module.getSpecification() ) != null )
			{
				// Nothing to do: already failed to identify this specification,
				// no use trying again
			}
			else
			{
				// Check to see if we've already identified it
				Module identifiedModule = identifiedModules.get( module.getSpecification() );
				if( identifiedModule == null )
				{
					if( phaser != null )
					{
						boolean alreadyIdentifying = !identifyModuleJobs.beginIfNotBegun( module.getSpecification(), executor, phaser, identifyModuleTask( module, recursive, phaser ) );
						if( alreadyIdentifying )
						{
							// Another thread is already in the process of
							// identifying this specification, so we'll wait for
							// them to finish
							final String id = begin( "Waiting for identification of " + module.getSpecification() );
							identifyModuleJobs.onEnd( module.getSpecification(), new Runnable()
							{
								public void run()
								{
									Module identifiedModule = identifiedModules.get( module.getSpecification() );
									if( identifiedModule != null )
										end( id, "Already identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
									else
										fail( id, "Could not identify " + module.getSpecification() );
								}
							} );
							return;
						}
					}

					String id = begin( "Identifying " + module.getSpecification() );

					// Gather allowed module identifiers from all repositories
					Set<ModuleIdentifier> allowedModuleIdentifiers = new LinkedHashSet<ModuleIdentifier>();
					for( Repository repository : context.getRepositories() )
						for( ModuleIdentifier allowedModuleIdentifier : repository.getAllowedModuleIdentifiers( module.getSpecification(), this ) )
							allowedModuleIdentifiers.add( allowedModuleIdentifier );

					// Pick the best module identifier
					if( !allowedModuleIdentifiers.isEmpty() )
					{
						LinkedList<ModuleIdentifier> moduleIdentifiers = new LinkedList<ModuleIdentifier>( allowedModuleIdentifiers );
						Collections.sort( moduleIdentifiers );

						// Best module is last (newest)
						ModuleIdentifier moduleIdentifier = moduleIdentifiers.getLast();
						identifiedModule = moduleIdentifier.getRepository().getModule( moduleIdentifier, this );

						if( identifiedModule != null )
							end( id, "Identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
						else
							fail( id, "Could not get module " + moduleIdentifier + " from " + moduleIdentifier.getRepository().getId() + " repository" );
					}
					else
						fail( id, "Could not identify " + module.getSpecification() );
				}
				else
				{
					debug( "Already identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
					identifiedCacheHits.incrementAndGet();
				}

				if( identifiedModule != null )
					module.copyIdentificationFrom( identifiedModule );
			}

			addModule( module );
		}

		identifyModuleJobs.end( module.getSpecification() );

		if( context.isRecursive() )
		{
			// Identify dependencies recursively
			if( phaser != null )
			{
				// Future
				for( Module dependency : module.getDependencies() )
					identifyModuleFuture( dependency, true, phaser );
			}
			else
			{
				// Do now
				for( Module dependency : module.getDependencies() )
					identifyModule( dependency, true, null );
			}
		}
		else
		{
			// Add dependencies as is (unidentified)
			for( Module dependency : module.getDependencies() )
				addModule( dependency );
		}
	}

	public Runnable identifyModuleTask( final Module module, final boolean recursive, final Phaser phaser )
	{
		return new Runnable()
		{
			public void run()
			{
				try
				{
					identifyModule( module, recursive, phaser );
				}
				catch( Throwable x )
				{
					error( "Identification error for " + module.getSpecification() + ": " + x.getMessage(), x );
				}
				phaser.arriveAndDeregister();
			}
		};
	}

	public Future<?> identifyModuleFuture( Module module, boolean recursive, Phaser phaser )
	{
		phaser.register();
		return executor.submit( identifyModuleTask( module, recursive, phaser ) );
	}

	public void findConflicts()
	{
		conflicts.find( getIdentifiedModules() );
	}

	public void resolveConflicts()
	{
		findConflicts();
		conflicts.resolve( getConflictPolicy(), this );
		for( Conflict conflict : getConflicts() )
			for( Module reject : conflict.getRejects() )
			{
				identifiedModules.remove( reject.getIdentifier() );
				replaceModule( reject, conflict.getChosen() );
			}
	}

	public Iterable<Artifact> install( String directory, boolean overwrite, boolean parallel )
	{
		return install( new File( directory ), overwrite, parallel );
	}

	public Iterable<Artifact> install( File directory, boolean overwrite, boolean parallel )
	{
		Collection<Artifact> artifacts = new ArrayList<Artifact>();

		String id = begin( "Installing" );

		Downloader downloader = new Downloader( getExecutor(), this );
		downloader.setDelay( 100 );
		for( Module module : identifiedModules )
			for( Artifact artifact : module.getIdentifier().getArtifacts( directory ) )
			{
				if( overwrite || !artifact.getFile().exists() )
					downloader.submit( artifact.getSourceUrl(), artifact.getFile(), module.getIdentifier().getRepository().validateFileTask( module.getIdentifier(), artifact.getFile(), this, downloader.getPhaser() ) );
				else
					module.getIdentifier().getRepository().validateFile( module.getIdentifier(), artifact.getFile(), this );
				artifacts.add( artifact );
			}
		downloader.waitUntilDone();

		end( id, "Installed " + downloader.getCount() + " artifacts" );

		return Collections.unmodifiableCollection( artifacts );
	}

	public void applyRules( Module module, IdentificationContext context )
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private String defaultPlatform = "maven";

	private ConflictPolicy conflictPolicy = ConflictPolicy.NEWEST;

	private final List<Module> explicitModules = new ArrayList<Module>();

	private final Collection<Repository> repositories = new ArrayList<Repository>();

	private final Collection<Rule> rules = new ArrayList<Rule>();

	private final Modules identifiedModules = new Modules();

	private final Modules unidentifiedModules = new Modules();

	private final Conflicts conflicts = new Conflicts();

	private final AtomicInteger identifiedCacheHits = new AtomicInteger();

	private final Jobs identifyModuleJobs = new Jobs();

	private final ExecutorService executor = Executors.newFixedThreadPool( 10, DaemonThreadFactory.INSTANCE );
}

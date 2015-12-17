package com.threecrickets.creel;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.internal.ConfigHelper;
import com.threecrickets.creel.internal.IdentificationContext;

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
		return Collections.unmodifiableCollection( identifiedModules );
	}

	public Iterable<Module> getUnidentifiedModules()
	{
		return Collections.unmodifiableCollection( unidentifiedModules );
	}

	public Iterable<Collection<Module>> getConflicts()
	{
		return Collections.unmodifiableCollection( conflicts );
	}

	public ForkJoinPool getForkJoinPool()
	{
		return forkJoinPool;
	}

	public int getIdentifiedCacheHits()
	{
		return identifiedCacheHits.get();
	}

	/**
	 * Gets an instance of a module (from identifiedModules) if it has already
	 * been identified.
	 * 
	 * @param moduleIdentifier
	 * @return
	 */
	public Module getIdentifiedModule( ModuleSpecification moduleSpecification )
	{
		identifiedModulesLock.lock();
		try
		{
			for( Module identifiedModule : identifiedModules )
				if( moduleSpecification.equals( identifiedModule.getSpecification() ) )
					return identifiedModule;
		}
		finally
		{
			identifiedModulesLock.unlock();
		}
		return null;
	}

	public void addIdentifiedModule( Module module )
	{
		ModuleIdentifier moduleIdentifier = module.getIdentifier();
		identifiedModulesLock.lock();
		try
		{
			boolean found = false;
			for( Module identifiedModule : identifiedModules )
				if( moduleIdentifier.compareTo( identifiedModule.getIdentifier() ) == 0 )
				{
					identifiedModule.merge( module );
					found = true;
					break;
				}
			if( !found )
				identifiedModules.add( module );
		}
		finally
		{
			identifiedModulesLock.unlock();
		}
	}

	public void removeIdentifiedModule( Module module )
	{
		ModuleIdentifier moduleIdentifier = module.getIdentifier();
		identifiedModulesLock.lock();
		try
		{
			for( Module identifiedModule : identifiedModules )
				if( moduleIdentifier.compareTo( identifiedModule.getIdentifier() ) == 0 )
				{
					identifiedModules.remove( moduleIdentifier );
					break;
				}
		}
		finally
		{
			identifiedModulesLock.unlock();
		}
	}

	public void addUnidentifiedModule( Module module )
	{
		ModuleIdentifier moduleIdentifier = module.getIdentifier();
		unidentifiedModulesLock.lock();
		try
		{
			boolean found = false;
			for( Module unidentifiedModule : unidentifiedModules )
				if( moduleIdentifier.compareTo( unidentifiedModule.getIdentifier() ) == 0 )
				{
					unidentifiedModule.merge( module );
					found = true;
					break;
				}
			if( !found )
				unidentifiedModules.add( module );
		}
		finally
		{
			unidentifiedModulesLock.unlock();
		}
	}

	public void addModule( Module module )
	{
		if( module.getIdentifier() != null )
			addIdentifiedModule( module );
		else
			addUnidentifiedModule( module );
	}

	public void replaceModule( Module oldModule, Module newModule )
	{
		for( ListIterator<Module> i = explicitModules.listIterator(); i.hasNext(); )
		{
			Module explicitModule = i.next();
			if( ( explicitModule.getIdentifier() != null ) && ( explicitModule.getIdentifier().compareTo( oldModule.getIdentifier() ) == 0 ) )
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
	public void identify()
	{
		String id = begin( "Identifying" );

		Collection<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
		for( Module explicitModule : explicitModules )
			tasks.add( identifyModuleTask( explicitModule, true ) );
		forkJoinPool.invokeAll( tasks );

		int count = 10;
		end( id, "Made " + count + " identifications" );
	}

	/**
	 * Identifies a module, optionally identifying its dependencies recursively
	 * (supporting fork/join parallelism).
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
	 */
	public void identifyModule( Module module, boolean recursive )
	{
		IdentificationContext context = new IdentificationContext();
		for( Repository repository : repositories )
			if( repository.isAll() )
				context.getRepositories().add( repository );

		applyRules( module, context );

		if( module.getIdentifier() != null )
		{
			// Already identified
		}
		else if( !context.isExclude() && module.getSpecification() != null )
		{
			// Check to see if we've already identified it
			Module identifiedModule = getIdentifiedModule( module.getSpecification() );
			if( identifiedModule == null )
			{
				// Gather allowed module identifiers from all repositories
				String id = begin( "Identifying " + module.getSpecification() );

				Collection<ModuleIdentifier> moduleIdentifiers = new ArrayList<ModuleIdentifier>();
				for( Repository repository : context.getRepositories() )
				{
					Iterable<ModuleIdentifier> allowedModuleIdentifiers = repository.getAllowedModuleIdentifiers( module.getSpecification(), this );

					// Note: the first repository to report an identifier will
					// "win," the following repositories will have their reports
					// discarded
					/*
					 * moduleIdentifiers =
					 * Sincerity.Objects.concatUnique(moduleIdentifiers,
					 * allowedModuleIdentifiers, function(moduleIdentifier1,
					 * moduleIdentifier2) { return
					 * moduleIdentifier1.compare(moduleIdentifier2) == 0; }
					 */
				}

				// Pick the best module identifier
				if( !moduleIdentifiers.isEmpty() )
				{
					/*
					 * moduleIdentifiers.sort(function(moduleIdentifier1,
					 * moduleIdentifier2) { // Reverse newness order return
					 * moduleIdentifier2.compare(moduleIdentifier1); })
					 */

					// Best module is first (newest)
					ModuleIdentifier identifiedModuleIdentifier = moduleIdentifiers.iterator().next();
					identifiedModule = identifiedModuleIdentifier.getRepository().getModule( identifiedModuleIdentifier, this );

					if( identifiedModule != null )
						end( id, "Identified " + identifiedModule.getIdentifier() + " in " + identifiedModule.getIdentifier().getRepository().getId() + " repository" );
					else
						fail( id, "Could not get module " + identifiedModuleIdentifier + " from " + identifiedModuleIdentifier.getRepository().getId() + " repository" );
				}
				else
					fail( id, "Could not identify " + module.getSpecification() );
			}

			if( identifiedModule != null )
				module.merge( identifiedModule );
		}
	}

	public Callable<Void> identifyModuleTask( final Module module, final boolean recursive )
	{
		return new Callable<Void>()
		{
			public Void call()
			{
				try
				{
					identifyModule( module, recursive );
				}
				catch( Exception x )
				{
					error( "Identification error for " + module.getSpecification() + ": " + x.getMessage(), x );
				}
				return null;
			}
		};
	}

	public void findConflicts()
	{
	}

	public void resolveConflicts()
	{
	}

	public void install( File directory, boolean overwrite, boolean parallel )
	{
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

	private final Collection<Module> identifiedModules = new ArrayList<Module>();;

	private final ReentrantLock identifiedModulesLock = new ReentrantLock();

	private final Collection<Module> unidentifiedModules = new ArrayList<Module>();;

	private final ReentrantLock unidentifiedModulesLock = new ReentrantLock();

	private final Collection<Collection<Module>> conflicts = new ArrayList<Collection<Module>>();;

	private final AtomicInteger identifiedCacheHits = new AtomicInteger();

	private final ForkJoinPool forkJoinPool = new ForkJoinPool( 10 );
}

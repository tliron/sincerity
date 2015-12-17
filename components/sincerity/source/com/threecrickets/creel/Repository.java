package com.threecrickets.creel;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.internal.ConfigHelper;

/**
 * Base class for repositories.
 * 
 * @author Tal Liron
 */
public abstract class Repository implements Cloneable
{
	//
	// Construction
	//

	public Repository( String id, boolean all, int parallelism )
	{
		this.id = id;
		this.all = all;
		executor = Executors.newFixedThreadPool( parallelism );
	}

	public Repository( Map<String, ?> config )
	{
		ConfigHelper configHelper = new ConfigHelper( config );
		id = configHelper.getString( "id" );
		all = configHelper.getBoolean( "all", true );
		int parallelism = configHelper.getInt( "parallelism", 5 );
		executor = Executors.newFixedThreadPool( parallelism );
	}

	//
	// Attributes
	//

	public String getId()
	{
		return id;
	}

	public boolean isAll()
	{
		return all;
	}

	public abstract boolean hasModule( ModuleIdentifier moduleIdentifier );

	public abstract Module getModule( ModuleIdentifier moduleIdentifier, Notifier notifier );

	public abstract Iterable<ModuleIdentifier> getAllowedModuleIdentifiers( ModuleSpecification moduleSpecification, Notifier notifier );

	//
	// Operations
	//

	public abstract void installModule( ModuleIdentifier moduleIdentifier, File directory, boolean overwrite, Notifier notifier );

	public Future<?> installModuleFuture( final ModuleIdentifier moduleIdentifier, final File directory, final boolean overwrite, final Notifier notifier )
	{
		return executor.submit( new Runnable()
		{
			public void run()
			{
				try
				{
					installModule( moduleIdentifier, directory, overwrite, notifier );
				}
				catch( Exception x )
				{
					notifier.error( "Install error for " + moduleIdentifier.toString() + ": " + x.getMessage(), x );
				}
			}
		} );
	}

	public abstract String applyModuleRule( Module module, Rule rule, Notifier notifier );

	//
	// Object
	//

	@Override
	public String toString()
	{
		return "id=" + id;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String id;

	private final boolean all;

	private final ExecutorService executor;
}

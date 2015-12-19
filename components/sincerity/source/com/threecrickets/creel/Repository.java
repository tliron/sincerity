package com.threecrickets.creel;

import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.internal.ConfigHelper;
import com.threecrickets.creel.internal.DaemonThreadFactory;

/**
 * Base class for repositories.
 * <p>
 * These are implemented differently per platform.
 * <p>
 * Child classes <b>must</b> override {@link Object#equals(Object)} and
 * {@link Object#hashCode()} with a proper implementation. The following
 * semantics are supported and recommended:
 * 
 * <pre>
 * &#64;Override
 * public boolean equals(Object object) {
 * 	if(!super.equals(object)) return false;
 * 	MyClass myObject = (MyClass) object;
 * 	return ...;
 * }
 * 
 * &#64;Override
 * public int hashCode() {
 * 	return Objects.hash(super.hashCode(), ...);
 * }
 * </pre>
 * 
 * Note that for the equals() override to work this way, we had to implement
 * dynamic class checking in the base class, like so:
 * 
 * <pre>
 * if((object == null) || (getClass() != object.getClass())) return false;
 * ...
 * </pre>
 * 
 * Likewise, we made sure that hashCode() in the base class properly hashes our
 * data fields, and never returns an arbitrary number in case there are no data
 * fields.
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
		this.parallelism = parallelism;
		executor = Executors.newFixedThreadPool( parallelism );
	}

	public Repository( Map<String, ?> config )
	{
		ConfigHelper configHelper = new ConfigHelper( config );
		id = configHelper.getString( "id" );
		all = configHelper.getBoolean( "all", true );
		parallelism = configHelper.getInt( "parallelism", 5 );
		executor = Executors.newFixedThreadPool( parallelism, DaemonThreadFactory.INSTANCE );
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

	public int getParallelism()
	{
		return parallelism;
	}

	public ExecutorService getExecutor()
	{
		return executor;
	}

	public abstract boolean hasModule( ModuleIdentifier moduleIdentifier );

	public abstract Module getModule( ModuleIdentifier moduleIdentifier, Notifier notifier );

	/**
	 * Expected to be sorted.
	 * 
	 * @param moduleSpecification
	 * @param notifier
	 * @return
	 */
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
				catch( Throwable x )
				{
					notifier.error( "Install error for " + moduleIdentifier.toString() + ": " + x.getMessage(), x );
				}
			}
		} );
	}

	public abstract String applyModuleRule( Module module, Rule rule, Notifier notifier );

	//
	// Cloneable
	//

	@Override
	public abstract Repository clone();

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		Repository repository = (Repository) object;
		return id.equals( repository.getId() ) && ( isAll() == repository.isAll() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( getId(), isAll() );
	}

	@Override
	public String toString()
	{
		return "id=" + getId();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String id;

	private final boolean all;

	private final int parallelism;

	private final ExecutorService executor;
}
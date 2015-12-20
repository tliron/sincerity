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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Phaser;

import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.util.ConfigHelper;

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
	// Classes
	//

	public class ValidateFile implements Runnable
	{
		public ValidateFile( ModuleIdentifier moduleIdentifier, File file, Notifier notifier, Phaser phaser )
		{
			this.moduleIdentifier = moduleIdentifier;
			this.file = file;
			this.notifier = notifier;
			this.phaser = phaser;
		}

		public void run()
		{
			try
			{
				validateFile( moduleIdentifier, file, notifier );
			}
			catch( Throwable x )
			{
				notifier.error( "Validation error for " + moduleIdentifier.toString() + ": " + x.getMessage(), x );
			}
			if( phaser != null )
				phaser.arriveAndDeregister();
		}

		final ModuleIdentifier moduleIdentifier;

		final File file;

		final Notifier notifier;

		final Phaser phaser;
	}

	//
	// Construction
	//

	public Repository( String id, boolean all )
	{
		this.id = id;
		this.all = all;
	}

	public Repository( Map<String, ?> config )
	{
		ConfigHelper configHelper = new ConfigHelper( config );
		id = configHelper.getString( "id" );
		all = configHelper.getBoolean( "all", true );
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

	public abstract void validateFile( ModuleIdentifier moduleIdentifier, File file, Notifier notifier );

	public ValidateFile validateFileTask( ModuleIdentifier moduleIdentifier, File file, Notifier notifier, Phaser phaser )
	{
		return new ValidateFile( moduleIdentifier, file, notifier, phaser );
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
}

package com.threecrickets.creel;

import java.io.File;
import java.util.Objects;

/**
 * Base class for module identifiers.
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
public abstract class ModuleIdentifier implements Comparable<ModuleIdentifier>, Cloneable
{
	//
	// Construction
	//

	public ModuleIdentifier( Repository repository )
	{
		super();
		this.repository = repository;
	}

	//
	// Attributes
	//

	public Repository getRepository()
	{
		return repository;
	}

	public abstract Iterable<Artifact> getArtifacts( File directory );

	//
	// Cloneable
	//

	@Override
	public abstract ModuleIdentifier clone();

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		ModuleIdentifier moduleIdentifier = (ModuleIdentifier) object;
		return getRepository().equals( moduleIdentifier.getRepository() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( getRepository() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Repository repository;
}

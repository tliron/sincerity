package com.threecrickets.creel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * Base class for module specification.
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
public abstract class ModuleSpecification implements Cloneable
{
	/**
	 * Checks whether a module identifier is allowed by the specification.
	 * 
	 * @param moduleIdentifier
	 * @return
	 */
	public abstract boolean allowsModuleIdentifier( ModuleIdentifier moduleIdentifier );

	/**
	 * Filters out those module identifiers that match the specification.
	 * 
	 * @param moduleIdentifiers
	 * @return
	 */
	public Iterable<ModuleIdentifier> filterAllowedModuleIdentifiers( Iterable<ModuleIdentifier> moduleIdentifiers )
	{
		Collection<ModuleIdentifier> allowedModuleIdentifiers = new ArrayList<ModuleIdentifier>();
		for( ModuleIdentifier moduleIdentifier : moduleIdentifiers )
			if( allowsModuleIdentifier( moduleIdentifier ) )
				allowedModuleIdentifiers.add( moduleIdentifier );
		return Collections.unmodifiableCollection( allowedModuleIdentifiers );
	}

	//
	// Cloneable
	//

	@Override
	public abstract ModuleSpecification clone();

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		return true;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash();
	}
}

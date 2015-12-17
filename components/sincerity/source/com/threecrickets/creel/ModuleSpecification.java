package com.threecrickets.creel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Base class for module specification.
 * <p>
 * These are implemented differently per platform.
 * 
 * @author Tal Liron
 */
public abstract class ModuleSpecification implements Cloneable
{
	public abstract boolean equals( ModuleSpecification moduleSpecification );

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
}

package com.threecrickets.creel;

/**
 * Base class for module identifiers.
 * <p>
 * These are implemented differently per platform.
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Repository repository;
}

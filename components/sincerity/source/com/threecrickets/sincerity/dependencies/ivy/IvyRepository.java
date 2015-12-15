/**
 * Copyright 2011-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.dependencies.ivy;

import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;

import com.threecrickets.sincerity.dependencies.Repository;
import com.threecrickets.sincerity.dependencies.ivy.internal.PyPiResolver;

/**
 * A wrapper around an Ivy {@link DependencyResolver}.
 * 
 * @author Tal Liron
 */
public class IvyRepository extends Repository
{
	//
	// Repository
	//

	@Override
	public String getName()
	{
		return dependencyResolver.getName();
	}

	@Override
	public String getType()
	{
		if( dependencyResolver instanceof IBiblioResolver )
			return "maven";
		else if( dependencyResolver instanceof PyPiResolver )
			return "pypi";
		else
			return null;
	}

	@Override
	public String getLocation()
	{
		if( dependencyResolver instanceof IBiblioResolver )
			return ( (IBiblioResolver) dependencyResolver ).getRoot();
		else if( dependencyResolver instanceof PyPiResolver )
			return ( (PyPiResolver) dependencyResolver ).getRoot();
		else
			return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected IvyRepository( DependencyResolver dependencyResolver )
	{
		this.dependencyResolver = dependencyResolver;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final DependencyResolver dependencyResolver;
}

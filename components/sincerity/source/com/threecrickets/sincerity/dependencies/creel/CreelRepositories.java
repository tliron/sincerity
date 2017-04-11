/**
 * Copyright 2011-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.dependencies.creel;

import java.util.ArrayList;
import java.util.Collection;

import com.threecrickets.sincerity.dependencies.Repositories;
import com.threecrickets.sincerity.dependencies.Repository;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * @author Tal Liron
 */
public class CreelRepositories extends Repositories
{
	//
	// Repositories
	//

	@Override
	public Collection<Repository> get( String section )
	{
		Collection<Repository> repositories = new ArrayList<Repository>();
		if( "public".equals( section ) )
			for( com.threecrickets.creel.Repository repository : container.engine.getRepositories() )
				repositories.add( new CreelRepository( repository ) );
		return repositories;
	}

	@Override
	public boolean addMaven( String section, String name, String url ) throws SincerityException
	{
		return false;
	}

	@Override
	public boolean addPyPi( String section, String name, String url ) throws SincerityException
	{
		return false;
	}

	@Override
	public boolean remove( String section, String name ) throws SincerityException
	{
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected CreelRepositories( CreelContainer container )
	{
		this.container = container;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final CreelContainer container;
}

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

import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.sincerity.dependencies.Repository;

/**
 * A wrapper around a Creel {@link com.threecrickets.creel.Repository}.
 * 
 * @author Tal Liron
 */
public class CreelRepository extends Repository
{
	//
	// Repository
	//

	@Override
	public String getName()
	{
		return repository.getId();
	}

	@Override
	public String getType()
	{
		if( repository instanceof MavenRepository )
			return "maven";
		return null;
	}

	@Override
	public String getLocation()
	{
		if( repository instanceof MavenRepository )
			return ( (MavenRepository) repository ).getUrl().toString();
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected CreelRepository( com.threecrickets.creel.Repository repository )
	{
		this.repository = repository;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final com.threecrickets.creel.Repository repository;
}

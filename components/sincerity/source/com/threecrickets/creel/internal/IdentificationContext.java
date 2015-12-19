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

package com.threecrickets.creel.internal;

import java.util.ArrayList;
import java.util.Collection;

import com.threecrickets.creel.Repository;

/**
 * @author Tal Liron
 */
public class IdentificationContext
{
	//
	// Construction
	//

	public IdentificationContext( Iterable<Repository> repositories, boolean recursive )
	{
		for( Repository repository : repositories )
			if( repository.isAll() )
				this.repositories.add( repository );
		this.recursive = recursive;
	}

	//
	// Attributes
	//

	public Collection<Repository> getRepositories()
	{
		return repositories;
	}

	public boolean isExclude()
	{
		return exclude;
	}

	public void setExclude( boolean exclude )
	{
		this.exclude = exclude;
	}

	public boolean isRecursive()
	{
		return recursive;
	}

	public void setRecursive( boolean recursive )
	{
		this.recursive = recursive;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Collection<Repository> repositories = new ArrayList<Repository>();

	private boolean exclude;

	private boolean recursive;
}

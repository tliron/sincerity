package com.threecrickets.creel.internal;

import java.util.ArrayList;
import java.util.Collection;

import com.threecrickets.creel.Repository;

public class IdentificationContext
{
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

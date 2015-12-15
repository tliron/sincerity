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

package com.threecrickets.sincerity.ivy;

import com.threecrickets.sincerity.License;

/**
 * @author Tal Liron
 */
public class IvyLicense extends License
{
	//
	// License
	//

	@Override
	public String getName()
	{
		return license.getName();
	}

	@Override
	public String getUrl()
	{
		return license.getUrl();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected IvyLicense( org.apache.ivy.core.module.descriptor.License license )
	{
		this.license = license;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final org.apache.ivy.core.module.descriptor.License license;
}

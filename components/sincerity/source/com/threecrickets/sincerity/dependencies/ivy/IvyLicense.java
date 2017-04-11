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

package com.threecrickets.sincerity.dependencies.ivy;

import java.net.MalformedURLException;
import java.net.URL;

import com.threecrickets.sincerity.dependencies.License;

/**
 * A wrapper around an Ivy {@link org.apache.ivy.core.module.descriptor.License}
 * .
 * 
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
	public URL getUrl()
	{
		try
		{
			return new URL( license.getUrl() );
		}
		catch( MalformedURLException x )
		{
			throw new RuntimeException( x );
		}
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

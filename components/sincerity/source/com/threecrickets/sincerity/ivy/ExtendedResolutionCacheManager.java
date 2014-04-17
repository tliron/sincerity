/**
 * Copyright 2011-2014 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.ivy;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.ivy.core.cache.DefaultResolutionCacheManager;

/**
 * Extends Ivy's default resolution cache manager to allow for separate
 * configuration of the root directory for resolution reports. In the default
 * implementation, the resolution cache root is used.
 * 
 * @author Tal Liron
 */
public class ExtendedResolutionCacheManager extends DefaultResolutionCacheManager
{
	//
	// Constants
	//

	// "resolved-[organisation]-[module]-[revision].xml"
	public static final String RESOLVED_IVY_PATTERN = "packages.conf";

	// "resolved-[organisation]-[module]-[revision].properties"
	public static final String RESOLVED_IVY_PROPERTIES_PATTERN = "resolution.conf";

	//
	// Construction
	//

	public ExtendedResolutionCacheManager( File cacheBaseDir, File reportBaseDir )
	{
		super( cacheBaseDir );
		resolveReportRoot = reportBaseDir;
		setResolvedIvyPattern( RESOLVED_IVY_PATTERN );
		setResolvedIvyPropertiesPattern( RESOLVED_IVY_PROPERTIES_PATTERN );
	}

	//
	// Attributes
	//

	public File getResolveReportRoot()
	{
		return resolveReportRoot;
	}

	//
	// ResolutionCacheManager
	//

	@Override
	public File getConfigurationResolveReportInCache( String resolveId, String conf )
	{
		return new File( getResolveReportRoot(), resolveId + "-" + conf + ".xml" );
	}

	@Override
	public File[] getConfigurationResolveReportsInCache( String resolveId )
	{
		final String prefix = resolveId + "-";
		final String suffix = ".xml";
		return getResolveReportRoot().listFiles( new FilenameFilter()
		{
			public boolean accept( File dir, String name )
			{
				return ( name.startsWith( prefix ) && name.endsWith( suffix ) );
			}
		} );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File resolveReportRoot;
}

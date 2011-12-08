package com.threecrickets.sincerity.ivy;

import java.io.File;
import java.io.FilenameFilter;

import org.apache.ivy.core.cache.DefaultResolutionCacheManager;

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

	public ExtendedResolutionCacheManager( File baseDir )
	{
		super( new File( baseDir, "cache/sincerity" ) );
		resolveReportRoot = new File( getResolutionCacheRoot(), "resolution" );
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

package com.threecrickets.sincerity.ivy;

import java.util.HashMap;
import java.util.Map;

import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;

public class SincerityRepositoryCacheManager extends DefaultRepositoryCacheManager
{
	//
	// Attributes
	//

	// Note: Ivy injects the settings using standard bean setters

	public String getBuilderIvyPattern()
	{
		return builderIvyPattern;
	}

	public void setBuilderIvyPattern( String builderIvyPattern )
	{
		this.builderIvyPattern = builderIvyPattern;
	}

	public String getBuilderEggDirPattern()
	{
		return builderEggDirPattern;
	}

	public void setBuilderEggDirPattern( String builderEggDirPattern )
	{
		this.builderEggDirPattern = builderEggDirPattern;
	}

	public String getBuilderSourceDirPattern()
	{
		return builderSourceDirPattern;
	}

	public void setBuilderSourceDirPattern( String builderSourceDirPattern )
	{
		this.builderSourceDirPattern = builderSourceDirPattern;
	}

	public void addConfiguredArtifactPattern( Map<?, ?> attributes )
	{
		String type = (String) attributes.remove( "type" );
		if( type == null )
			throw new IllegalArgumentException( "'type' attribute is mandatory for artifactPattern" );
		String pattern = (String) attributes.remove( "pattern" );
		if( pattern == null )
			throw new IllegalArgumentException( "'pattern' attribute is mandatory for artifactPattern" );
		artifactPatterns.put( type, pattern );
	}

	//
	// DefaultRepositoryCacheManager
	//

	@Override
	public String getArchivePathInCache( Artifact artifact, ArtifactOrigin origin )
	{
		if( isOriginalMetadataArtifact( artifact ) )
			return super.getArchivePathInCache( artifact, origin );

		String pattern = artifactPatterns.get( artifact.getType() );
		if( pattern == null )
			pattern = getArtifactPattern();
		return IvyPatternHelper.substitute( pattern, artifact, origin );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private String builderIvyPattern;

	private String builderEggDirPattern;

	private String builderSourceDirPattern;

	private Map<String, String> artifactPatterns = new HashMap<String, String>();

	private boolean isOriginalMetadataArtifact( Artifact artifact )
	{
		return artifact.isMetadata() && artifact.getType().endsWith( ".original" );
	}
}

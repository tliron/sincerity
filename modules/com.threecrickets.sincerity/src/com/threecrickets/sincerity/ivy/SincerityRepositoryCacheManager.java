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

	public String getCreatedIvyPattern()
	{
		return createdIvyPattern;
	}

	public void setCreatedIvyPattern( String createdIvyPattern )
	{
		this.createdIvyPattern = createdIvyPattern;
	}

	public String getCreatedEggPattern()
	{
		return createdEggPattern;
	}

	public void setCreatedEggPattern( String createdEggPattern )
	{
		this.createdEggPattern = createdEggPattern;
	}

	public String getUnpackedArchivePattern()
	{
		return unpackedArchivePattern;
	}

	public void setUnpackedArchivePattern( String unpackedArchivePattern )
	{
		this.unpackedArchivePattern = unpackedArchivePattern;
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

	private String createdIvyPattern;

	private String createdEggPattern;

	private String unpackedArchivePattern;

	private Map<String, String> artifactPatterns = new HashMap<String, String>();

	private boolean isOriginalMetadataArtifact( Artifact artifact )
	{
		return artifact.isMetadata() && artifact.getType().endsWith( ".original" );
	}
}

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

package com.threecrickets.sincerity.dependencies.ivy;

import java.io.File;

import com.threecrickets.sincerity.dependencies.Artifact;

/**
 * Wrapper around an Ivy {@link org.apache.ivy.core.module.descriptor.Artifact}.
 * 
 * @author Tal Liron
 */
public class IvyArtifact extends Artifact
{
	//
	// Artifact
	//

	@Override
	public String getName()
	{
		return artifact.getName();
	}

	@Override
	public String getExtension()
	{
		return artifact.getExt();
	}

	@Override
	public String getType()
	{
		return artifact.getType();
	}

	@Override
	public File getLocation()
	{
		String location = artifact.getId().getAttribute( "location" );
		return location != null ? new File( location ) : null;
	}

	@Override
	public Integer getSize()
	{
		String size = artifact.getId().getAttribute( "size" );
		return size != null ? new Integer( size ) : null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected IvyArtifact( org.apache.ivy.core.module.descriptor.Artifact artifact )
	{
		this.artifact = artifact;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final org.apache.ivy.core.module.descriptor.Artifact artifact;
}

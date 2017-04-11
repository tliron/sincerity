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

import java.io.File;

import com.threecrickets.sincerity.dependencies.Artifact;

/**
 * A wrapper around a Creel {@link com.threecrickets.creel.Artifact}.
 * 
 * @author Tal Liron
 */
public class CreelArtifact extends Artifact
{
	//
	// Artifact
	//

	@Override
	public String getName()
	{
		String name = artifact.getFile().getName();
		int period = name.lastIndexOf( '.' );
		return period != -1 ? name.substring( 0, period ) : name;
	}

	@Override
	public String getExtension()
	{
		String name = artifact.getFile().getName();
		int period = name.lastIndexOf( '.' );
		return period != -1 ? name.substring( period + 1 ) : "";
	}

	@Override
	public String getType()
	{
		return getExtension();
	}

	@Override
	public File getLocation()
	{
		return artifact.getFile();
	}

	@Override
	public Integer getSize()
	{
		long length = artifact.getFile().length();
		return length != 0L ? (int) length : null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected CreelArtifact( com.threecrickets.creel.Artifact artifact )
	{
		this.artifact = artifact;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final com.threecrickets.creel.Artifact artifact;
}

/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.threecrickets.sincerity.exception.UnpackingException;
import com.threecrickets.sincerity.internal.FileUtil;

/**
 * @author Tal Liron
 */
public class Artifact implements Comparable<Artifact>
{
	//
	// Construction
	//

	public Artifact( File file, URL url, boolean isVolatile, Container container )
	{
		this.file = file;
		this.url = url;
		this.isVolatile = isVolatile;
		this.container = container;
		path = container.getRelativePath( file );
	}

	//
	// Attributes
	//

	/**
	 * The artifact's intended absolute location in the filesystem.
	 * 
	 * @return The file
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * The artifact's intended location in the filesystem relative to the
	 * container root.
	 * 
	 * @return The path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * This URL points to the read-only original source for this artifact. This
	 * is often a "jar:" URL, meaning that the artifact was or will be extracted
	 * from a Jar file.
	 * 
	 * @return The origin URL
	 */
	public URL getUrl()
	{
		return url;
	}

	public boolean isVolatile()
	{
		return isVolatile;
	}

	/**
	 * Checks if the artifact exists in the filesystem and if its contents are
	 * identical to that of the origin URL.
	 * 
	 * @return True if file is different from its origin
	 * @throws UnpackingException
	 */
	public boolean isDifferent() throws UnpackingException
	{
		try
		{
			return !FileUtil.isSameContent( url, file );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not compare artifact " + file + " to " + url, x );
		}
	}

	//
	// Operations
	//

	/**
	 * Copies the artifact from its origin URL to its intended location in the
	 * filesystem.
	 * 
	 * @param filter
	 * @param overwrite
	 *        Whether to overwrite the file if it already exists
	 * @throws UnpackingException
	 */
	public void install( String filter, boolean overwrite ) throws UnpackingException
	{
		if( file.exists() )
		{
			// TODO: back up in cache if overwriting!!!

			if( overwrite )
			{
				if( container.getSincerity().getVerbosity() >= 2 )
					container.getSincerity().getOut().println( "Installing artifact (overwriting): " + path );
			}
			else
			{
				if( container.getSincerity().getVerbosity() >= 2 )
					container.getSincerity().getOut().println( "Not installing modified artifact: " + path );
				return;
			}
		}
		else
		{
			if( container.getSincerity().getVerbosity() >= 2 )
				container.getSincerity().getOut().println( "Installing artifact: " + path );
		}

		try
		{
			file.getParentFile().mkdirs();
			org.apache.ivy.util.FileUtil.copy( url, file, null );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not copy artifact from " + url + " to " + file, x );
		}
	}

	//
	// Comparable
	//

	public int compareTo( Artifact o )
	{
		return file.compareTo( o.file );
	}

	//
	// Object
	//

	@Override
	public int hashCode()
	{
		return file.hashCode();
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o )
			return true;
		if( !( o instanceof Artifact ) )
			return false;
		return file.equals( ( (Artifact) o ).file );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private final URL url;

	private final boolean isVolatile;

	private final Container container;

	private final String path;
}

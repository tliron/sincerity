/**
 * Copyright 2011-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnpackingException;
import com.threecrickets.sincerity.internal.IoUtil;

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
		this.originUrl = url;
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

	public byte[] getFileDigest() throws IOException
	{
		if( fileDigest == null )
			fileDigest = IoUtil.getDigest( new BufferedInputStream( new FileInputStream( file ) ) );
		return fileDigest;
	}

	/**
	 * This URL points to the read-only original source for this artifact. This
	 * is often a "jar:" URL, meaning that the artifact was or will be extracted
	 * from a Jar file.
	 * 
	 * @return The origin URL
	 */
	public URL getOriginUrl()
	{
		return originUrl;
	}

	public byte[] getOriginDigest() throws IOException
	{
		if( ( originDigest == null ) && ( originUrl != null ) )
			originDigest = IoUtil.getDigest( new BufferedInputStream( originUrl.openStream() ) );
		return originDigest;
	}

	public boolean isVolatile()
	{
		return isVolatile;
	}

	/**
	 * Checks if the artifact's unpacked contents are identical to that of the
	 * original.
	 * 
	 * @param managedArtifacts
	 *        The managed artifacts
	 * @return True if file is different from its original
	 * @throws UnpackingException
	 */
	public boolean isDifferent( ManagedArtifacts managedArtifacts ) throws UnpackingException
	{
		try
		{
			byte[] originalDigest = managedArtifacts.getOriginalDigest( this );
			if( originalDigest == null )
				return true;
			return !Arrays.equals( getFileDigest(), originalDigest );
		}
		catch( SincerityException x )
		{
			throw new UnpackingException( "Could not compare artifact digest: " + file, x );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not compare artifact digest: " + file, x );
		}
	}

	//
	// Operations
	//

	/**
	 * Copies the artifact from its origin URI to its intended location in the
	 * filesystem.
	 * 
	 * @param managedArtifacts
	 *        The managed artifacts
	 * @param overwrite
	 *        Whether to overwrite the file if it already exists
	 * @throws SincerityException
	 */
	public void unpack( ManagedArtifacts managedArtifacts, boolean overwrite ) throws SincerityException
	{
		// Don't reinstall volatile artifacts that were already installed
		if( isVolatile && managedArtifacts.wasInstalled( this ) )
			return;

		if( file.exists() )
		{
			if( isDifferent( managedArtifacts ) )
			{
				if( overwrite )
				{
					originDigest = null;

					if( container.getSincerity().getVerbosity() >= 1 )
						container.getSincerity().getOut().println( "Unpacking over changed artifact: " + path );

					// TODO: backup changed artifacts in cache!
				}
				else
				{
					if( container.getSincerity().getVerbosity() >= 1 )
						container.getSincerity().getOut().println( "Artifact has been changed, so not overwriting: " + path );
					return;
				}
			}
			else
			{
				if( container.getSincerity().getVerbosity() >= 2 )
					container.getSincerity().getOut().println( "Artifact unchanged, so no need to unpack: " + path );
				return;
			}
		}
		else
		{
			if( container.getSincerity().getVerbosity() >= 2 )
				container.getSincerity().getOut().println( "Unpacking artifact: " + path );
		}

		// Unpack
		try
		{
			InputStream in = new BufferedInputStream( originUrl.openStream() );
			try
			{
				file.getParentFile().mkdirs();
				fileDigest = null;
				OutputStream out = new BufferedOutputStream( new FileOutputStream( file ) );
				try
				{
					IoUtil.copy( in, out );
				}
				finally
				{
					out.close();
				}
			}
			finally
			{
				in.close();
			}

			if( !Arrays.equals( getFileDigest(), getOriginDigest() ) )
				throw new UnpackingException( "Artifact incorrectly unpacked from " + originUrl + " to " + file );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not copy artifact from " + originUrl + " to " + file + ": " + x.getMessage(), x );
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

	@Override
	public String toString()
	{
		return file.toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private final URL originUrl;

	private final boolean isVolatile;

	private final Container container;

	private final String path;

	private byte[] fileDigest;

	private byte[] originDigest;

}

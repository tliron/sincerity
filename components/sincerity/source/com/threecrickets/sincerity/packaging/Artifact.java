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

package com.threecrickets.sincerity.packaging;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

import com.threecrickets.creel.util.DigestUtil;
import com.threecrickets.creel.util.IoUtil;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnpackingException;

/**
 * @author Tal Liron
 */
public class Artifact implements Comparable<Artifact>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param file
	 *        The intended absolute destination in the filesystem
	 * @param originUrl
	 *        The origin URL
	 * @param isVolatile
	 *        True if volatile
	 * @param packagingContext
	 *        The packagingContext
	 */
	public Artifact( File file, URL originUrl, boolean isVolatile, PackagingContext packagingContext )
	{
		this.file = file;
		this.originUrl = originUrl;
		this.isVolatile = isVolatile;
		this.packagingContext = packagingContext;
		path = packagingContext.getRelativePath( file );
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
	 * packagingContext root.
	 * 
	 * @return The path
	 */
	public String getPath()
	{
		return path;
	}

	/**
	 * The cached digest for the file.
	 * 
	 * @return The digest
	 * @throws SincerityException
	 *         In case of an error
	 */
	public byte[] getFileDigest() throws SincerityException
	{
		try
		{
			if( ( fileDigest == null ) && file.exists() )
				fileDigest = DigestUtil.getDigest( file, "SHA-1" );
			return fileDigest;
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not calculate file digest for " + file + ": " + x.getMessage(), x );
		}
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

	/**
	 * The cached digest for the origin.
	 * 
	 * @return The digest
	 * @throws SincerityException
	 *         In case of an error
	 */
	public byte[] getOriginDigest() throws SincerityException
	{
		try
		{
			if( ( originDigest == null ) && ( originUrl != null ) )
				originDigest = DigestUtil.getDigest( originUrl, "SHA-1" );
			return originDigest;
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not calculate origin digest for " + originUrl + ": " + x.getMessage(), x );
		}
	}

	/**
	 * True if volatile.
	 * 
	 * @return True if volatile
	 */
	public boolean isVolatile()
	{
		return isVolatile;
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
	 * @param verify
	 *        Whether to verify the unpacking
	 * @return The digest
	 * @throws SincerityException
	 *         In case of an error
	 */
	public byte[] unpack( ArtifactManager managedArtifacts, boolean overwrite, boolean verify ) throws SincerityException
	{
		// Don't reinstall volatile artifacts that were already installed
		if( isVolatile && managedArtifacts.wasInstalled( this ) )
		{
			if( packagingContext.getVerbosity() >= 2 )
				packagingContext.getOut().println( "Volatile artifact already unpacked once, so not overwriting: " + path );
			return managedArtifacts.getOriginalDigest( this );
		}

		byte[] currentDigest = getFileDigest();
		byte[] newDigest = getOriginDigest();

		if( currentDigest != null )
		{
			// The file already exists
			boolean hasNewVersion = !Arrays.equals( currentDigest, newDigest );
			if( hasNewVersion )
			{
				// There is a new version of the artifact
				byte[] originalDigest = managedArtifacts.getOriginalDigest( this );
				boolean changedByUser = !Arrays.equals( currentDigest, originalDigest );
				if( changedByUser )
				{
					// The current version of the artifact has been changed by
					// the user
					if( overwrite )
					{
						if( packagingContext.getVerbosity() >= 1 )
							packagingContext.getOut().println( "Unpacking over changed artifact: " + path );

						// TODO: backup changed-by-user artifacts in cache!
					}
					else
					{
						if( packagingContext.getVerbosity() >= 1 )
							packagingContext.getOut().println( "Artifact has been changed, so not overwriting: " + path );
						return originalDigest;
					}
				}
				else
				{
					if( packagingContext.getVerbosity() >= 1 )
						packagingContext.getOut().println( "Unpacking new version of artifact: " + path );
				}
			}
			else
			{
				if( packagingContext.getVerbosity() >= 2 )
					packagingContext.getOut().println( "Artifact already unpacked: " + path );
				return currentDigest;
			}
		}
		else
		{
			if( packagingContext.getVerbosity() >= 2 )
				packagingContext.getOut().println( "Unpacking artifact: " + path );
		}

		// Unpack
		try
		{
			IoUtil.copy( originUrl, file, null );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not copy artifact from " + originUrl + " to " + file + ": " + x.getMessage(), x );
		}
		fileDigest = null;

		// Verify
		if( verify )
		{
			if( !Arrays.equals( getFileDigest(), getOriginDigest() ) )
				throw new UnpackingException( "Artifact incorrectly unpacked from " + originUrl + " to " + file );
		}

		return getFileDigest();
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

	private final PackagingContext packagingContext;

	private final String path;

	private byte[] fileDigest;

	private byte[] originDigest;
}

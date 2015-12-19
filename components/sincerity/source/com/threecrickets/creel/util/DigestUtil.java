/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Tal Liron
 */
public abstract class DigestUtil
{
	//
	// Static operations
	//

	/**
	 * Calculates a digest for the content.
	 * 
	 * @param content
	 *        The content
	 * @param algorithm
	 *        The algorithm
	 * @return The digest
	 * @throws IOException
	 *         In case the algorithm is not found
	 */
	public static byte[] getDigest( byte[] content, String algorithm ) throws IOException
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance( algorithm );
			digest.reset();
			digest.update( content );
			return digest.digest();
		}
		catch( NoSuchAlgorithmException x )
		{
			IOException io = new IOException();
			io.initCause( x );
			throw io;
		}
	}

	/**
	 * Calculates a digest for a stream.
	 * <p>
	 * Note that the stream is closed by this method!
	 * 
	 * @param stream
	 *        The stream
	 * @param algorithm
	 *        The algorithm
	 * @return The digest
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static byte[] getDigest( InputStream stream, String algorithm ) throws IOException
	{
		try
		{
			MessageDigest digest = MessageDigest.getInstance( algorithm );
			digest.reset();
			byte[] buffer = new byte[IoUtil.BUFFER_SIZE];
			int length = 0;
			while( ( length = stream.read( buffer ) ) != -1 )
				digest.update( buffer, 0, length );
			return digest.digest();
		}
		catch( NoSuchAlgorithmException x )
		{
			IOException io = new IOException();
			io.initCause( x );
			throw io;
		}
		finally
		{
			stream.close();
		}
	}

	/**
	 * Calculates a digest for a file.
	 * 
	 * @param file
	 *        The file
	 * @param algorithm
	 *        The algorithm
	 * @return The digest
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static byte[] getDigest( File file, String algorithm ) throws IOException
	{
		return getDigest( new FileInputStream( file ), algorithm );
	}

	/**
	 * Calculates a digest for a URL.
	 * 
	 * @param url
	 *        The URL
	 * @param algorithm
	 *        The algorithm
	 * @return The digest
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static byte[] getDigest( URL url, String algorithm ) throws IOException
	{
		return getDigest( url.openStream(), algorithm );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private DigestUtil()
	{
	}
}

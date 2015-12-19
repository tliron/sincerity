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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @author Tal Liron
 */
public abstract class IoUtil
{
	//
	// Constants
	//

	public static final int BUFFER_SIZE = 16 * 1024;

	public interface ProgressListener
	{
		public void onProgress( int position, int length );
	}

	//
	// Static operations
	//

	/**
	 * Converts a URL to a file if it has the "file:" spec.
	 * 
	 * @param url
	 *        The URL
	 * @return The file or null
	 */
	public static File toFile( URL url )
	{
		if( "file".equalsIgnoreCase( url.getProtocol() ) )
		{
			try
			{
				return new File( url.toURI() );
			}
			catch( URISyntaxException x )
			{
				// This should never happen
			}
		}
		return null;
	}

	/**
	 * True if the URL points to a reachable resource.
	 * 
	 * @param url
	 *        The URL
	 * @return True if valid
	 */
	public static boolean isValid( URL url )
	{
		try
		{
			url.openStream().close();
			return true;
		}
		catch( IOException x )
		{
			return false;
		}
	}

	/**
	 * Copies entire channels.
	 * 
	 * @param source
	 *        The source channel
	 * @param target
	 *        The target channel
	 * @param progressListener
	 *        The progress listener or null
	 * @param length
	 *        The source length or -1 if unknown (used for progress listener)
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( ReadableByteChannel source, WritableByteChannel target, ProgressListener progressListener, int length ) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate( BUFFER_SIZE );
		int count, position = 0;
		while( ( count = source.read( buffer ) ) != -1 )
		{
			buffer.flip();
			while( buffer.hasRemaining() )
				target.write( buffer );
			buffer.clear();
			position += count;
			if( progressListener != null )
				progressListener.onProgress( position, length );
		}
	}

	/**
	 * Copies entire streams.
	 * 
	 * @param source
	 *        The source input stream
	 * @param target
	 *        The target output stream
	 * @param progressListener
	 *        The progress listener or null
	 * @param length
	 *        The source length or -1 if unknown (used for progress listener)
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( InputStream source, OutputStream target, ProgressListener progressListener, int length ) throws IOException
	{
		byte[] buffer = new byte[BUFFER_SIZE];
		int count, position = 0;
		while( ( count = source.read( buffer ) ) != -1 )
		{
			target.write( buffer, 0, count );
			position += count;
			if( progressListener != null )
				progressListener.onProgress( position, length );
		}
	}

	/**
	 * Copies an entire stream to a file at a specific location.
	 * 
	 * @param source
	 *        The source input stream
	 * @param target
	 *        The target random-access file
	 * @param start
	 *        The start position in the file
	 * @param progressListener
	 *        The progress listener or null
	 * @param length
	 *        The source length or -1 if unknown (used for progress listener)
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( InputStream source, File target, int start, ProgressListener progressListener, int length ) throws IOException
	{
		RandomAccessFile random = new RandomAccessFile( target, "rw" );
		try
		{
			random.seek( start );
			byte[] buffer = new byte[BUFFER_SIZE];
			int count, position = 0;
			while( ( count = source.read( buffer ) ) != -1 )
			{
				random.write( buffer, 0, count );
				position += count;
				if( progressListener != null )
					progressListener.onProgress( position, length );
			}
		}
		finally
		{
			random.close();
		}
	}

	/**
	 * Copies all data from a URL to a file, creating necessary parent
	 * directories at destination.
	 * <p>
	 * Will detect "file:" URLs and optimize accordingly.
	 * 
	 * @param source
	 *        The source URL
	 * @param target
	 *        The target file
	 * @param progressListener
	 *        The progress listener or null
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( URL source, File target, ProgressListener progressListener ) throws IOException
	{
		File file = toFile( source );
		if( file != null )
		{
			// Optimize for file copies
			copy( file, target );
			return;
		}

		InputStream in = source.openStream();
		try
		{
			Files.createDirectories( target.toPath().getParent() );
			OutputStream out = new FileOutputStream( target );
			try
			{
				copy( in, out, progressListener, source.openConnection().getContentLength() );
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
	}

	/**
	 * Copies a file, creating necessary parent directories at destination.
	 * 
	 * @param source
	 *        The source file
	 * @param target
	 *        The target file
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copy( File source, File target ) throws IOException
	{
		Files.createDirectories( target.toPath().getParent() );
		Files.copy( source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING );
	}

	/**
	 * Reads all bytes from a URL. If you know the URL is of a file and you
	 * don't absolutely need an array of bytes, use
	 * {@link IoUtil#readBuffer(File)}, which is more efficient.
	 * 
	 * @param url
	 *        The URL
	 * @param progressListener
	 *        The progress listener or null
	 * @return The bytes
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static byte[] readBytes( URL url, ProgressListener progressListener ) throws IOException
	{
		File file = toFile( url );
		if( file != null )
			// Use readBytes(File) if possible, because it is more efficient
			// (because we know the buffer size in advance)
			return readBytes( file );

		ReadableByteChannel fromChannel = Channels.newChannel( url.openStream() );
		try
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream( BUFFER_SIZE );
			WritableByteChannel toChannel = Channels.newChannel( buffer );
			copy( fromChannel, toChannel, progressListener, url.openConnection().getContentLength() );
			return buffer.toByteArray();
		}
		finally
		{
			fromChannel.close();
		}
	}

	/**
	 * Reads all bytes from a file. If you don't absolutely need an array of
	 * bytes, use {@link IoUtil#readBuffer(File)}, which is more efficient.
	 * 
	 * @param file
	 *        The file
	 * @return The bytes
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static byte[] readBytes( File file ) throws IOException
	{
		FileInputStream input = new FileInputStream( file );
		try
		{
			FileChannel channel = input.getChannel();
			try
			{
				byte[] bytes = new byte[(int) channel.size()];
				channel.read( ByteBuffer.wrap( bytes ) );
				return bytes;
			}
			finally
			{
				channel.close();
			}
		}
		catch( FileNotFoundException x )
		{
			return null;
		}
		finally
		{
			input.close();
		}
	}

	/**
	 * Reads all bytes from a file as a memory-mapped buffer. This is more
	 * efficient than {@link IoUtil#readBytes(File)}.
	 * 
	 * @param file
	 *        The file
	 * @return The bytes
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static ByteBuffer readBuffer( File file ) throws IOException
	{
		FileInputStream input = new FileInputStream( file );
		try
		{
			FileChannel channel = input.getChannel();
			try
			{
				return channel.map( FileChannel.MapMode.READ_ONLY, 0, channel.size() );
			}
			finally
			{
				channel.close();
			}
		}
		catch( FileNotFoundException x )
		{
			return null;
		}
		finally
		{
			input.close();
		}
	}

	/**
	 * Reads all UTF-8 text from a URL.
	 * 
	 * @param url
	 *        The URL
	 * @param progressListener
	 *        The progress listener or null
	 * @return The content
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static String readText( URL url, ProgressListener progressListener ) throws IOException
	{
		File file = toFile( url );
		if( file != null )
			// Use readText(File) if possible, because it is more efficient
			// (because we know the buffer size in advance)
			return readText( file );

		return new String( readBytes( url, progressListener ), StandardCharsets.UTF_8 );
	}

	/**
	 * Reads all UTF-8 text from a file.
	 * 
	 * @param file
	 *        The file
	 * @return The file's content
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static String readText( File file ) throws IOException
	{
		return StandardCharsets.UTF_8.decode( readBuffer( file ) ).toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private IoUtil()
	{
	}
}

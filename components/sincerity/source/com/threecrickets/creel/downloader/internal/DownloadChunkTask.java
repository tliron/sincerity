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

package com.threecrickets.creel.downloader.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.util.IoUtil;

/**
 * @author Tal Liron
 */
public class DownloadChunkTask extends DownloaderTask implements IoUtil.ProgressListener
{
	//
	// Construction
	//

	public DownloadChunkTask( Downloader downloader, Runnable validator, URL url, File file, int start, int length, int chunk, int chunks, AtomicInteger counter )
	{
		super( downloader, validator );
		this.url = url;
		this.file = file;
		this.start = start;
		this.length = length;
		this.chunk = chunk;
		this.chunks = chunks;
		this.counter = counter;
	}

	//
	// Attributes
	//

	public URL getUrl()
	{
		return url;
	}

	public File getFile()
	{
		return file;
	}

	public int getStart()
	{
		return start;
	}

	public int getLength()
	{
		return length;
	}

	public int getChunk()
	{
		return chunk;
	}

	public int getChunks()
	{
		return chunks;
	}

	public AtomicInteger getCounter()
	{
		return counter;
	}

	//
	// Runnable
	//

	public void run()
	{

		id = getDownloader().getNotifier().begin( "Downloading from " + getUrl() + " (" + getChunk() + "/" + getChunks() + ")" );
		try
		{
			URLConnection connection = getUrl().openConnection();
			connection.setRequestProperty( "Range", "bytes=" + getStart() + "-" + ( getStart() + getLength() ) );
			IoUtil.copy( connection.getInputStream(), getFile(), getStart(), this, getLength() );
			getDownloader().getNotifier().end( id, "Downloaded to " + getFile() + " (" + getChunk() + "/" + getChunks() + ")" );
		}
		catch( IOException x )
		{
			getDownloader().getNotifier().error( x );
			getDownloader().getNotifier().fail( id, "Could not download from " + getUrl() + " (" + getChunk() + "/" + getChunks() + ")" );
		}
		done( getCounter() );
	}

	//
	// IoUtil.Listener
	//

	public void onProgress( int position, int length )
	{
		if( length > 0 )
			getDownloader().getNotifier().update( id, (double) position / (double) length );
		if( getDownloader().getDelay() > 0 )
		{
			try
			{
				Thread.sleep( getDownloader().getDelay() );
			}
			catch( InterruptedException e )
			{
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL url;

	private final File file;

	private final int start;

	private final int length;

	private final int chunk;

	private final int chunks;

	private final AtomicInteger counter;

	private String id;
}

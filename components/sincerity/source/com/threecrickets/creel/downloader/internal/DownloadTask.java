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
import java.net.URL;

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.util.IoUtil;

/**
 * @author Tal Liron
 */
public class DownloadTask extends DownloaderTask implements IoUtil.ProgressListener
{
	//
	// Construction
	//

	public DownloadTask( Downloader downloader, Runnable validator, URL url, File file )
	{
		super( downloader, validator );
		this.url = url;
		this.file = file;
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

	//
	// Runnable
	//

	public void run()
	{
		id = getDownloader().getNotifier().begin( "Downloading from " + getUrl() );
		try
		{
			IoUtil.copy( getUrl(), getFile(), this );
			getDownloader().getNotifier().end( id, "Downloaded to " + getFile() );
		}
		catch( Exception x )
		{
			getDownloader().getNotifier().error( x );
			getDownloader().getNotifier().fail( id, "Could not download from " + getUrl() );
		}
		done();
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

	private String id;
}

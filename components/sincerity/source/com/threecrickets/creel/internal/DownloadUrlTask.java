package com.threecrickets.creel.internal;

import java.io.File;
import java.net.URL;

import com.threecrickets.sincerity.util.IoUtil;

/**
 * @author Tal Liron
 */
public class DownloadUrlTask extends DownloadTask
{
	//
	// Construction
	//

	public DownloadUrlTask( Downloader downloader, Runnable validator, URL url, File file )
	{
		super( downloader, validator );
		this.url = url;
		this.file = file;
	}

	//
	// Runnable
	//

	public void run()
	{
		String id = getDownloader().getNotifier().begin( "Downloading from " + url );
		try
		{
			IoUtil.copy( url, file );
			getDownloader().getNotifier().end( id, "Downloaded to " + file );
		}
		catch( Exception x )
		{
			getDownloader().getNotifier().error( x );
			getDownloader().getNotifier().fail( id, "Could not download from " + url );
		}
		getDownloader().getPhaser().arriveAndDeregister();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL url;

	private final File file;
}

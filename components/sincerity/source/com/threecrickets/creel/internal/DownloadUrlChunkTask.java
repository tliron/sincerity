package com.threecrickets.creel.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URLConnection;

import com.threecrickets.sincerity.util.IoUtil;

/**
 * @author Tal Liron
 */
public class DownloadUrlChunkTask extends DownloadTask
{
	//
	// Construction
	//

	public DownloadUrlChunkTask( Downloader downloader, Runnable validator, URL url, File file, int rangeStart, int rangeEnd, int chunk, int chunks )
	{
		super( downloader, validator );
		this.url = url;
		this.file = file;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;
		this.chunk = chunk;
		this.chunks = chunks;
	}

	//
	// Runnable
	//

	public void run()
	{

		String id = getDownloader().getNotifier().begin( "Downloading from " + url + " (" + chunk + "/" + chunks + ")" );
		try
		{
			URLConnection connection = url.openConnection();
			connection.setRequestProperty( "Range", "bytes=" + rangeStart + "-" + rangeEnd );
			RandomAccessFile file = new RandomAccessFile( this.file, "rw" );
			try
			{
				IoUtil.copy( connection.getInputStream(), file, rangeStart );
			}
			finally
			{
				file.close();
			}
			getDownloader().getNotifier().end( id, "Downloaded to " + this.file + " (" + chunk + "/" + chunks + ")" );
		}
		catch( IOException x )
		{
			getDownloader().getNotifier().error( x );
			getDownloader().getNotifier().fail( id, "Could not download from " + url + " (" + chunk + "/" + chunks + ")" );
		}
		getDownloader().getPhaser().arriveAndDeregister();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL url;

	private final File file;

	private final int rangeStart;

	private final int rangeEnd;

	private final int chunk;

	private final int chunks;
}

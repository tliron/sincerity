package com.threecrickets.creel.internal;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

import com.threecrickets.creel.event.Notifier;

/**
 * @author Tal Liron
 */
public class Downloader
{
	//
	// Construction
	//

	public Downloader( ExecutorService executor, Notifier notifier )
	{
		this.executor = executor;
		this.notifier = notifier;
	}

	//
	// Attributes
	//

	public ExecutorService getExecutor()
	{
		return executor;
	}

	public Notifier getNotifier()
	{
		return notifier;
	}

	public Phaser getPhaser()
	{
		return phaser;
	}

	//
	// Operations
	//

	public void submit( URL sourceUrl, File file, Runnable validator )
	{
		try
		{
			Files.createDirectories( file.toPath().getParent() );
		}
		catch( IOException x )
		{
			getNotifier().error( "Could not create file " + file, x );
			return;
		}

		if( "file".equalsIgnoreCase( sourceUrl.getProtocol() ) )
		{
			// Optimize for file copies
			try
			{
				File sourceFile = new File( sourceUrl.toURI() );
				getPhaser().register();
				getExecutor().submit( new DownloadFileTask( this, validator, sourceFile, file ) );
			}
			catch( URISyntaxException x )
			{
			}
		}
		else
		{
			try
			{
				URLConnection connection = sourceUrl.openConnection();
				boolean supportsChunks = connection.getHeaderField( "Accept-Ranges" ).equals( "bytes" );
				int streamSize = 0;
				if( supportsChunks )
				{
					streamSize = connection.getContentLength();
					if( streamSize == -1 )
						supportsChunks = false;
				}

				if( supportsChunks )
				{
					// We support chunks
					int chunkCount = 4;
					int chunkSize = streamSize / chunkCount;
					for( int chunk = 0; chunk < chunkCount; chunk++ )
					{
						int start = chunk * chunkSize;
						int end = chunk < chunkCount - 1 ? start + chunkSize : streamSize;
						getPhaser().register();
						getExecutor().submit( new DownloadUrlChunkTask( this, null, sourceUrl, file, start, start + end, chunk + 1, chunkCount ) );
					}
				}
				else
				{
					// We don't support chunks
					getPhaser().register();
					getExecutor().submit( new DownloadUrlTask( this, validator, sourceUrl, file ) );
				}
			}
			catch( IOException x )
			{
				getNotifier().error( x );
			}
		}
	}

	public void waitUntilDone()
	{
		phaser.arriveAndAwaitAdvance();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ExecutorService executor;

	private final Notifier notifier;

	private final Phaser phaser = new Phaser( 1 );
}

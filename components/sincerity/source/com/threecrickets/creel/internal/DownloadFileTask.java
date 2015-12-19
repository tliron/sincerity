package com.threecrickets.creel.internal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * @author Tal Liron
 */
public class DownloadFileTask extends DownloadTask
{
	//
	// Construction
	//

	public DownloadFileTask( Downloader downloader, Runnable validator, File fromFile, File toFile )
	{
		super( downloader, validator );
		this.fromFile = fromFile;
		this.toFile = toFile;
	}

	//
	// Runnable
	//

	public void run()
	{
		String id = getDownloader().getNotifier().begin( "Copying file from " + fromFile );
		try
		{
			Files.createDirectories( toFile.toPath().getParent() );
			Files.copy( fromFile.toPath(), toFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
			getDownloader().getNotifier().end( id, "Copied file to " + toFile );
		}
		catch( IOException x )
		{
			getDownloader().getNotifier().error( x );
			getDownloader().getNotifier().fail( id, "Could not copy file from " + fromFile );
		}
		getDownloader().getPhaser().arriveAndDeregister();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File fromFile;

	private final File toFile;
}

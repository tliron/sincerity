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

import com.threecrickets.creel.downloader.Downloader;
import com.threecrickets.creel.util.IoUtil;

/**
 * @author Tal Liron
 */
public class CopyFileTask extends DownloaderTask
{
	//
	// Construction
	//

	public CopyFileTask( Downloader downloader, Runnable validator, File fromFile, File toFile )
	{
		super( downloader, validator );
		this.fromFile = fromFile;
		this.toFile = toFile;
	}

	//
	// Attributes
	//

	public File getFromFile()
	{
		return fromFile;
	}

	public File getToFile()
	{
		return toFile;
	}

	//
	// Runnable
	//

	public void run()
	{
		String id = getDownloader().getNotifier().begin( "Copying file from " + getFromFile() );
		try
		{
			IoUtil.copy( getFromFile(), getToFile() );
			getDownloader().getNotifier().end( id, "Copied file to " + getToFile() );
		}
		catch( IOException x )
		{
			getDownloader().getNotifier().error( x );
			getDownloader().getNotifier().fail( id, "Could not copy file from " + getFromFile() );
		}
		done();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File fromFile;

	private final File toFile;
}

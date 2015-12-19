package com.threecrickets.creel.internal;

/**
 * @author Tal Liron
 */
public abstract class DownloadTask implements Runnable
{
	//
	// Construction
	//

	public DownloadTask( Downloader downloader, Runnable validator )
	{
		this.downloader = downloader;
		this.validator = validator;
	}

	//
	// Attributes
	//

	public Downloader getDownloader()
	{
		return downloader;
	}

	public Runnable getValidator()
	{
		return validator;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Downloader downloader;

	private final Runnable validator;
}

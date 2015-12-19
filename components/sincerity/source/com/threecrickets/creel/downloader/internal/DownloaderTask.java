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

import java.util.concurrent.atomic.AtomicInteger;

import com.threecrickets.creel.downloader.Downloader;

/**
 * @author Tal Liron
 */
public abstract class DownloaderTask implements Runnable
{
	//
	// Construction
	//

	public DownloaderTask( Downloader downloader, Runnable validator )
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
	// Protected

	protected void done()
	{
		if( getValidator() != null )
		{
			getDownloader().getPhaser().register();
			getDownloader().getExecutor().submit( getValidator() );
		}
		getDownloader().incrementCount();
		getDownloader().getPhaser().arriveAndDeregister();
	}

	protected void done( AtomicInteger counter )
	{
		if( counter.decrementAndGet() == 0 )
			done();
		else
			getDownloader().getPhaser().arriveAndDeregister();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Downloader downloader;

	private final Runnable validator;
}

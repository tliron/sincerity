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

package com.threecrickets.creel.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author Tal Liron
 */
public class DaemonThreadFactory implements ThreadFactory
{
	//
	// Constants
	//

	public static final DaemonThreadFactory INSTANCE = new DaemonThreadFactory();

	//
	// ThreadFactory
	//

	public Thread newThread( Runnable runnable )
	{
		Thread thread = Executors.defaultThreadFactory().newThread( runnable );
		thread.setDaemon( true );
		return thread;
	}
}

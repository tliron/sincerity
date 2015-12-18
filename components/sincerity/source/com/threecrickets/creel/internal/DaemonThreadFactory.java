package com.threecrickets.creel.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

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

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

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

import com.threecrickets.creel.Manager;
import com.threecrickets.creel.Module;
import com.threecrickets.creel.internal.DaemonThreadFactory;
import com.threecrickets.creel.util.Jobs;

/**
 * @author Tal Liron
 */
public class ConcurrentIdentificationContext implements Closeable
{
	//
	// Construction
	//

	public ConcurrentIdentificationContext( int threads )
	{
		executor = Executors.newFixedThreadPool( threads, DaemonThreadFactory.INSTANCE );
	}

	//
	// Attributes
	//

	public ExecutorService getExecutor()
	{
		return executor;
	}

	public Phaser getPhaser()
	{
		return phaser;
	}

	public Jobs getJobs()
	{
		return jobs;
	}

	//
	// Operations
	//

	public void identifyModule( Manager.IdentifyModule identifyModule )
	{
		getPhaser().register();
		getExecutor().submit( identifyModule );
	}

	public boolean beginIdentifyingIfNotIdentifying( Module module, Manager.IdentifyModule identifyModule )
	{
		return getJobs().beginIfNotBegun( module.getSpecification(), getExecutor(), getPhaser(), identifyModule );
	}

	public void onIdentified( Module module, Manager.IdentifiedModule identifiedModule )
	{
		getJobs().onEnd( module.getSpecification(), identifiedModule );
	}

	public void notifyIdentified( Module module )
	{
		getJobs().notifyEnd( module.getSpecification() );
	}

	public void identified()
	{
		getPhaser().arriveAndDeregister();
	}

	//
	// Closeable
	//

	public void close()
	{
		phaser.arriveAndAwaitAdvance();
		executor.shutdown();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ExecutorService executor;

	private final Phaser phaser = new Phaser( 1 );

	private final Jobs jobs = new Jobs();
}

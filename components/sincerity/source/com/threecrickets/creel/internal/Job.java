package com.threecrickets.creel.internal;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * @author Tal Liron
 */
public class Job
{
	//
	// Construction
	//

	public Job( ExecutorService executor )
	{
		this.executor = executor;
	}

	//
	// Operations
	//

	public void onEnd( Runnable onEnd )
	{
		this.onEnd.add( onEnd );
	}

	public void end()
	{
		for( Runnable onEnd : this.onEnd )
			executor.submit( onEnd );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ExecutorService executor;

	private final Collection<Runnable> onEnd = new CopyOnWriteArrayList<Runnable>();
}

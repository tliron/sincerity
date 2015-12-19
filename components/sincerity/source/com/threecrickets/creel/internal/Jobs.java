package com.threecrickets.creel.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

/**
 * @param <T>
 */
public class Jobs
{
	//
	// Operations
	//

	public synchronized void clear()
	{
		jobs.clear();
	}

	public synchronized boolean beginIfNotBegun( Object token, ExecutorService executor, Phaser phaser, Runnable onEnd )
	{
		Job job = jobs.get( token.toString() );
		if( job == null )
		{
			// New job
			jobs.put( token.toString(), new Job( executor ) );
			return true;
		}
		else
		{
			// Another thread has already started this job, so let's wait until
			// they're done
			if( phaser != null )
				phaser.register();
			job.onEnd( onEnd );
			return false;
		}
	}

	public synchronized boolean end( Object token )
	{
		Job job = jobs.remove( token.toString() );
		if( job != null )
		{
			job.end();
			return true;
		}
		else
			return false;
	}

	public synchronized void onEnd( Object token, Runnable onEnd )
	{
		Job job = jobs.get( token.toString() );
		if( job != null )
			// Do later
			job.onEnd( onEnd );
		else
			// Do now
			onEnd.run();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Map<String, Job> jobs = new HashMap<String, Job>();
}

package com.threecrickets.creel.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
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

	public void clear()
	{
		jobs.clear();
	}

	public boolean begin( Object token, ExecutorService executor, Phaser phaser, Runnable onEnd )
	{
		Job newJob = new Job( executor );
		Job existingJob = jobs.putIfAbsent( token.toString(), newJob );
		if( existingJob != null )
		{
			// Another thread has already started this job, so let's wait until
			// they're done
			if( phaser != null )
				phaser.register();
			existingJob.onEnd( onEnd );
			return false;
		}
		return true;
	}

	public boolean end( Object token )
	{
		Job job = jobs.remove( token.toString() );
		if( job != null )
		{
			job.end();
			return true;
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private ConcurrentMap<String, Job> jobs = new ConcurrentHashMap<String, Job>();
}

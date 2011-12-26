package com.threecrickets.sincerity.internal;

public class ProcessDestroyer extends Thread
{
	//
	// Construction
	//

	public ProcessDestroyer( Process process )
	{
		super();
		this.process = process;
	}

	//
	// Thread
	//

	@Override
	public void run()
	{
		process.destroy();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Process process;
}

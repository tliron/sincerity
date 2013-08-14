/**
 * Copyright 2011-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.internal;

/**
 * Destroys a {@link Process}.
 * <p>
 * Useful in conjunction with {@link Runtime#addShutdownHook(Thread)}.
 * 
 * @author Tal Liron
 */
public class ProcessDestroyer extends Thread
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param process
	 *        The process
	 */
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

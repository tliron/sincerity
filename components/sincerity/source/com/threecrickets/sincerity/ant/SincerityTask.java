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

package com.threecrickets.sincerity.ant;

import java.util.ArrayList;

import org.apache.tools.ant.Task;

import com.threecrickets.sincerity.Sincerity;

/**
 * Sincerity Ant task.
 * <p>
 * Experimental! Would not properly support rebooting.
 * 
 * @author Tal Liron
 */
public class SincerityTask extends Task
{
	//
	// Task
	//

	@Override
	public void execute()
	{
		ArrayList<String> arguments = new ArrayList<String>();
		for( Arg arg : args )
			arguments.add( arg.value );
		Sincerity.main( arguments.toArray( new String[arguments.size()] ) );
	}

	public Arg createArg()
	{
		Arg arg = new Arg();
		args.add( arg );
		return arg;
	}

	public static class Arg
	{
		public void setValue( String value )
		{
			this.value = value;
		}

		private String value;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ArrayList<Arg> args = new ArrayList<Arg>();
}

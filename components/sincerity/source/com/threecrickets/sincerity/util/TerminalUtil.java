/**
 * Copyright 2011-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.util;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;

import jline.DefaultTerminal2;
import jline.Terminal;
import jline.Terminal2;
import jline.TerminalFactory;
import jline.UnixTerminal;
import jline.internal.Configuration;

/**
 * Terminal utilities.
 * 
 * @author Tal Liron
 */
public class TerminalUtil
{
	//
	// Static operations
	//

	/**
	 * {@link #createPrintWriter(PrintStream)} with {@link System#out}.
	 * 
	 * @return A new print writer
	 */
	public static PrintWriter createPrintWriter()
	{
		return createPrintWriter( System.out );
	}

	/**
	 * Creates an auto-flushing print writer on top of a
	 * <a href="https://github.com/jline/jline2">JLine</a> terminal if possible.
	 * 
	 * @param printStream
	 *        The print stream
	 * @return A new print writer
	 */
	public static PrintWriter createPrintWriter( PrintStream printStream )
	{
		try
		{
			// Try to use JLine

			Terminal terminal = TerminalFactory.get();
			if( !( terminal instanceof Terminal2 ) )
				terminal = new DefaultTerminal2( terminal );

			String encoding = terminal.getOutputEncoding();
			if( encoding == null )
				encoding = Configuration.getEncoding();

			if( terminal instanceof UnixTerminal )
				( (UnixTerminal) terminal ).enableInterruptCharacter();

			return new PrintWriter( new OutputStreamWriter( terminal.wrapOutIfNeeded( printStream ), encoding ), true );
		}
		catch( IOException x )
		{
			return new PrintWriter( new OutputStreamWriter( printStream ), true );
		}
	}

	public static void reset()
	{
		try
		{
			TerminalFactory.get().restore();
			// TerminalFactory.reset();
		}
		catch( Exception x )
		{
			x.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private TerminalUtil()
	{
	}
}

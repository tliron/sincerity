/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.internal;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

public class StringUtil
{
	public static String join( String[] strings, String delimiter )
	{
		StringBuilder r = new StringBuilder();
		for( int i = 0, length = strings.length - 1; i <= length; i++ )
		{
			r.append( strings[i] );
			if( i < length )
				r.append( delimiter );
		}
		return r.toString();
	}

	public static String join( Iterable<String> strings, String delimiter )
	{
		StringBuilder r = new StringBuilder();
		for( Iterator<String> i = strings.iterator(); i.hasNext(); )
		{
			r.append( i.next() );
			if( i.hasNext() )
				r.append( delimiter );
		}
		return r.toString();
	}

	public static String joinStackTrace( Throwable x )
	{
		StringWriter writer = null;
		writer = new StringWriter();
		joinStackTrace( x, writer );
		return writer.toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static void joinStackTrace( Throwable x, StringWriter writer )
	{
		PrintWriter printer = new PrintWriter( writer );
		while( x != null )
		{
			printer.println( x );
			StackTraceElement[] trace = x.getStackTrace();
			for( int i = 0; i < trace.length; i++ )
				printer.println( "\tat " + trace[i] );

			x = x.getCause();
			if( x != null )
				printer.println( "Caused by:\r\n" );
		}
	}
}

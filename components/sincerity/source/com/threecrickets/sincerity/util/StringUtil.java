/**
 * Copyright 2011-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

/**
 * String utilities.
 * 
 * @author Tal Liron
 */
public abstract class StringUtil
{
	//
	// Static operations
	//

	/**
	 * Converts an array of objects to an array of strings by calling
	 * {@link Object#toString()} on each.
	 * 
	 * @param objects
	 *        An array of objects
	 * @return An array of strings
	 */
	public static String[] toStringArray( Object[] objects )
	{
		int length = objects.length;
		String[] strings = new String[length];
		for( int i = 0; i < length; i++ )
		{
			Object object = objects[i];
			strings[i] = object != null ? object.toString() : null;
		}
		return strings;
	}

	/**
	 * Joins a list strings into a single string.
	 * 
	 * @param strings
	 *        The list of strings
	 * @param delimiter
	 *        The delimiter between strings
	 * @return The joined string
	 */
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

	/**
	 * Joins a list strings into a single string.
	 * 
	 * @param strings
	 *        The list of strings
	 * @param delimiter
	 *        The delimiter between strings
	 * @return The joined string
	 */
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

	/**
	 * Creates a human-readable stack trace.
	 * 
	 * @param x
	 *        The exception
	 * @return The stack trace
	 */
	public static String createHumanReadableStackTrace( Throwable x )
	{
		StringWriter writer = null;
		writer = new StringWriter();
		createHumanReadableStackTrace( x, writer );
		return writer.toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private StringUtil()
	{
	}

	/**
	 * Creates a human-readable stack trace.
	 * 
	 * @param x
	 *        The exception
	 * @param writer
	 *        The string writer
	 * @return The stack trace
	 */
	private static void createHumanReadableStackTrace( Throwable x, StringWriter writer )
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

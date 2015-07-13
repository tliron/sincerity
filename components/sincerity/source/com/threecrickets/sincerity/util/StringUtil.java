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

	/**
	 * Creates a hexadecimal representation for an array of bytes.
	 * <p>
	 * The letters A-F are in uppercase.
	 * 
	 * @param bytes
	 *        The bytes
	 * @return The hexadecimal representation
	 */
	public static String toHex( byte[] bytes )
	{
		// BigInteger i = new BigInteger( 1, bytes );
		// return String.format( "%0" + ( bytes.length << 1 ) + "x", i );

		// See: http://stackoverflow.com/a/9855338/849021

		char[] hexChars = new char[bytes.length * 2];
		int v;
		for( int j = 0; j < bytes.length; j++ )
		{
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX[v >>> 4];
			hexChars[j * 2 + 1] = HEX[v & 0x0F];
		}
		return new String( hexChars );
	}

	/**
	 * Creates an array of bytes from its hexadecimal representation.
	 * 
	 * @param hex
	 *        The hexadecimal representation
	 * @return The bytes
	 */
	public static byte[] fromHex( String hex )
	{
		int length = hex.length();
		byte[] data = new byte[length / 2];
		for( int i = 0; i < length; i += 2 )
			data[i / 2] = (byte) ( ( Character.digit( hex.charAt( i ), 16 ) << 4 ) + Character.digit( hex.charAt( i + 1 ), 16 ) );
		return data;
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

	private static final char[] HEX =
	{
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
}

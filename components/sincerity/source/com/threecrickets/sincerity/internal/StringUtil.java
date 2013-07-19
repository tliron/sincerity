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

	public static byte[] fromHex( String hex )
	{
		int length = hex.length();
		byte[] data = new byte[length / 2];
		for( int i = 0; i < length; i += 2 )
		{
			data[i / 2] = (byte) ( ( Character.digit( hex.charAt( i ), 16 ) << 4 ) + Character.digit( hex.charAt( i + 1 ), 16 ) );
		}
		return data;
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	final protected static char[] HEX =
	{
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};

	private StringUtil()
	{
	}
}

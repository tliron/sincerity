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

package com.threecrickets.sincerity.exception;

import com.threecrickets.sincerity.Command;

/**
 * Signifies that a Sincerity command did not get the arguments it expected.
 * 
 * @author Tal Liron
 */
public class BadArgumentsCommandException extends CommandException
{
	//
	// Construction
	//

	public BadArgumentsCommandException( Command command, String... argumentDescriptions )
	{
		super( command, createMessage( command, argumentDescriptions ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private static String createMessage( Command command, String[] argumentDescriptions )
	{
		StringBuilder s = new StringBuilder( "Command " );
		s.append( '"' );
		s.append( command );
		s.append( "\" requires: " );
		for( int length = argumentDescriptions.length, i = 0; i < length; i++ )
		{
			s.append( '[' );
			s.append( argumentDescriptions[i] );
			s.append( ']' );
			if( i < length - 1 )
				s.append( ' ' );
		}
		return s.toString();
	}
}

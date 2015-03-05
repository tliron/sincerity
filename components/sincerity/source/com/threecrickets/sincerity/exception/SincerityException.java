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

package com.threecrickets.sincerity.exception;

/**
 * The base of the Sincerity exception hierarchy.
 * 
 * @author Tal Liron
 */
public class SincerityException extends Exception
{
	//
	// Construction
	//

	public SincerityException( String message )
	{
		super( message );
	}

	public SincerityException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public SincerityException( Throwable cause )
	{
		super( cause );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

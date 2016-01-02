/**
 * Copyright 2011-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.exception;

import com.threecrickets.scripturian.Executable;

/**
 * Signifies that Sincerity encountered a Scripturian error.
 * 
 * @author Tal Liron
 * @see Executable#enter(Object, String, Object...)
 */
public class ScripturianException extends SincerityException
{
	//
	// Construction
	//

	public ScripturianException( String message )
	{
		super( message );
	}

	public ScripturianException( String message, Throwable cause )
	{
		super( message, cause );
	}

	public ScripturianException( Throwable cause )
	{
		super( cause );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

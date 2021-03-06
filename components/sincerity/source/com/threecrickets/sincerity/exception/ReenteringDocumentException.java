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

package com.threecrickets.sincerity.exception;

import com.threecrickets.scripturian.Executable;

/**
 * Signifies that Sincerity tried to enter a Scripturian document that was
 * already entered.
 * 
 * @author Tal Liron
 * @see Executable#enter(Object, String, Object...)
 */
public class ReenteringDocumentException extends ScripturianException
{
	//
	// Construction
	//

	public ReenteringDocumentException( String message )
	{
		super( message );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

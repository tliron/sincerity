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

import com.threecrickets.sincerity.Dependencies;

/**
 * Signifies that dependencies could not be installed.
 * 
 * @author Tal Liron
 * @see Dependencies#install(boolean)
 */
public class InstallationException extends SincerityException
{
	//
	// Construction
	//

	public InstallationException( String message )
	{
		super( message );
	}

	public InstallationException( String message, Throwable cause )
	{
		super( message, cause );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

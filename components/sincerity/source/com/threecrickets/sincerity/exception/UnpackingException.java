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

import com.threecrickets.sincerity.Artifact;

/**
 * Signifies that an {@link Artifact} could not extracted from its archive.
 * 
 * @author Tal Liron
 * @see Artifact#install(String, boolean)
 */
public class UnpackingException extends InstallationException
{
	//
	// Construction
	//

	public UnpackingException( String message )
	{
		super( message );
	}

	public UnpackingException( String message, Throwable cause )
	{
		super( message, cause );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

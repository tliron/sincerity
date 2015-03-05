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

import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Sincerity;

/**
 * Signifies that Sincerity is not running with a {@link Container}.
 * <p>
 * While Sincerity can run without a container, in some cases a container is
 * required, and this except can signify that there this condition has not been
 * met.
 * 
 * @author Tal Liron
 * @see Sincerity#setContainerRoot(java.io.File)
 */
public class NoContainerException extends SincerityException
{
	//
	// Construction
	//

	public NoContainerException( String message )
	{
		super( message );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

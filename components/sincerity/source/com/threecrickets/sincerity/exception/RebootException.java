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

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.sincerity.Sincerity;

/**
 * Signifies that Sincerity is in the process of rebooting, and that no further
 * operations should take place in the current instance.
 * <p>
 * Rebooting is handled via the {@link Bootstrap} mechanism.
 * 
 * @author Tal Liron
 * @see Sincerity#reboot()
 */
public class RebootException extends SincerityException
{
	//
	// Construction
	//

	public RebootException()
	{
		super( "Rebooting" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

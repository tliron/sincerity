/**
 * Copyright 2011-2012 Three Crickets LLC.
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
 * Signifies that a Sincerity command is not supported by any plugin.
 * 
 * @author Tal Liron
 */
public class UnknownCommandException extends CommandException
{
	//
	// Construction
	//

	public UnknownCommandException( Command command )
	{
		super( command, "Unknown command: " + command );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

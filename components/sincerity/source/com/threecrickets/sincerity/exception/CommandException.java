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

import com.threecrickets.sincerity.Command;

/**
 * Signifies that there was a problem executing a Sincerity command.
 * 
 * @author Tal Liron
 */
public class CommandException extends SincerityException
{
	//
	// Construction
	//

	public CommandException( Command command, String message )
	{
		super( message );
		this.command = command;
	}

	public CommandException( Command command, String message, Throwable cause )
	{
		super( message, cause );
		this.command = command;
	}

	//
	// Attributes
	//

	public Command getCommand()
	{
		return command;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Command command;
}

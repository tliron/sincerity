package com.threecrickets.sincerity.exception;

import com.threecrickets.sincerity.Command;

public class CommandException extends Exception
{
	//
	// Construction
	//

	public CommandException( Command command, String message )
	{
		super( message );
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

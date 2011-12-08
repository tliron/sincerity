package com.threecrickets.sincerity.exception;

public class CommandException extends Exception
{
	//
	// Construction
	//

	public CommandException( String command, String message )
	{
		super( message );
		this.command = command;
	}

	//
	// Attributes
	//

	public String getCommand()
	{
		return command;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final String command;
}

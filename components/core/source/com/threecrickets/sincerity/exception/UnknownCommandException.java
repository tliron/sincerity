package com.threecrickets.sincerity.exception;

public class UnknownCommandException extends CommandException
{
	//
	// Construction
	//

	public UnknownCommandException( String command )
	{
		super( command, "Unknown command: " + command );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

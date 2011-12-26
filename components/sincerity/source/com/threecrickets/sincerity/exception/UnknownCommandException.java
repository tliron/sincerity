package com.threecrickets.sincerity.exception;

import com.threecrickets.sincerity.Command;

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

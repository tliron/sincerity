package com.threecrickets.sincerity.exception;

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

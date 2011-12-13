package com.threecrickets.sincerity.exception;

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

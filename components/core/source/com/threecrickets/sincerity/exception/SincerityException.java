package com.threecrickets.sincerity.exception;

public class SincerityException extends Exception
{
	//
	// Construction
	//

	public SincerityException( String message )
	{
		super( message );
	}

	public SincerityException( String message, Throwable cause )
	{
		super( message, cause );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

package com.threecrickets.sincerity.exception;

public class UnknownShortcutException extends SincerityException
{
	//
	// Construction
	//

	public UnknownShortcutException( String shortcut )
	{
		super( "Unknown shortcut: " + shortcut );
		this.shortcut = shortcut;
	}

	//
	// Attributes
	//

	public String getShortcut()
	{
		return shortcut;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final String shortcut;
}

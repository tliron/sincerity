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

import com.threecrickets.sincerity.Shortcuts;

/**
 * Signifies that a Sincerity shortcut has been referred to, but is not defined
 * by this container.
 * 
 * @author Tal Liron
 * @see Shortcuts
 */
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

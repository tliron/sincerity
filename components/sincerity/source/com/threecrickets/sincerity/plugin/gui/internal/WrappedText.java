/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin.gui.internal;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;

/**
 * Displays auto-wrapped text.
 * 
 * @author Tal Liron
 */
public class WrappedText extends JTextArea
{
	//
	// Construction
	//

	public WrappedText( String text )
	{
		super( text );
		setLineWrap( true );
		setWrapStyleWord( true );
		setAlignmentX( 0 );
		setEditable( false );
		setBackground( null );
		setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

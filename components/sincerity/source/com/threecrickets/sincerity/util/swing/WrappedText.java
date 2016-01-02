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

package com.threecrickets.sincerity.util.swing;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.border.Border;

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
		setBorder( border );
		setBackground( background );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private static final Border border = BorderFactory.createEmptyBorder();

	// See:
	// http://stackoverflow.com/questions/613603/java-nimbus-laf-with-transparent-text-fields
	private static final Color background = new Color( 0, 0, 0, 0 );
}

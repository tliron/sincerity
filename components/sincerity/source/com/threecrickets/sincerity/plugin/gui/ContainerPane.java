/**
 * Copyright 2011-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin.gui;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * A pane that appears at the top of the Sincerity GUI and shows information
 * about the current container if such is in use.
 * 
 * @author Tal Liron
 */
public class ContainerPane extends JPanel
{
	//
	// Construction
	//

	public ContainerPane( Sincerity sincerity ) throws SincerityException
	{
		super( new FlowLayout( FlowLayout.LEADING, 10, 0 ) );

		JLabel label = new JLabel();
		label.setHorizontalAlignment( SwingConstants.LEADING );
		add( label );

		try
		{
			label.setText( "Using container at: " + sincerity.getContainer().getRoot() );
		}
		catch( NoContainerException x )
		{
			label.setText( "You are not using a container. Do you want to create one?" );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

/**
 * Copyright 2011-2014 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.DependenciesPlugin;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;

/**
 * A button for adding and installing dependencies to a container.
 * 
 * @author Tal Liron
 * @see DependenciesPlugin
 * @see AddDependenciesDialog
 */
public class AddDependenciesButton extends JButton implements ActionListener
{
	//
	// Construction
	//

	public AddDependenciesButton( Sincerity sincerity )
	{
		super( "Add and Install" );

		this.sincerity = sincerity;

		addActionListener( this );
	}

	//
	// ActionListener
	//

	public void actionPerformed( ActionEvent event )
	{
		try
		{
			new AddDependenciesDialog( sincerity );
		}
		catch( SincerityException x )
		{
			GuiUtil.error( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Sincerity sincerity;
}

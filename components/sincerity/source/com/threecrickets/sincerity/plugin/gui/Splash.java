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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;

/**
 * Sinceirty splash screen.
 * 
 * @author Tal Liron
 */
public class Splash extends JWindow
{
	//
	// Construction
	//

	public Splash( Runnable runnable )
	{
		super();

		this.runnable = runnable;

		JLabel title = new JLabel( "Starting Sincerity...", SwingConstants.CENTER );
		title.setBorder( BorderFactory.createEmptyBorder( 20, 20, 0, 20 ) );

		JPanel panel = new JPanel();
		panel.setBackground( Color.WHITE );
		panel.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
		panel.setLayout( new BorderLayout() );
		panel.add( title, BorderLayout.NORTH );
		panel.add( new JLabel( new ImageIcon( Splash.class.getResource( "sincerity.png" ) ) ), BorderLayout.CENTER );

		getContentPane().add( panel );

		setAlwaysOnTop( true );
		setPreferredSize( new Dimension( 350, 350 ) );
		GuiUtil.center( this );
		setVisible( true );
	}

	//
	// Container
	//

	@Override
	public void paint( Graphics g )
	{
		super.paint( g );

		if( runnable != null )
		{
			final Runnable toRun = runnable;
			runnable = null;
			SwingUtilities.invokeLater( new Runnable()
			{
				public void run()
				{
					try
					{
						toRun.run();
					}
					finally
					{
						dispose();
					}
				}
			} );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private Runnable runnable;
}

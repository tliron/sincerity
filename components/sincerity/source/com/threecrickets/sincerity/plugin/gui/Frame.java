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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;
import com.threecrickets.sincerity.plugin.gui.internal.LazyTabbedPane;

/**
 * The main frame for the Sincerity GUI.
 * <p>
 * Plugins can extend this GUI by implementing {@link Plugin1#gui(Command)}. The
 * recommended content areas for adding new components are {@link #getTabs()}
 * and {@link #getToolbar()}.
 * 
 * @author Tal Liron
 * @see Sincerity#getFrame()
 * @see Plugin1#gui(Command)
 */
public class Frame extends JFrame implements Runnable
{
	//
	// Construction
	//

	public Frame( Sincerity sincerity ) throws SincerityException
	{
		super( "Sincerity " + sincerity.getVersion().get( "version" ) );

		this.sincerity = sincerity;

		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		setIconImage( new ImageIcon( Frame.class.getResource( "sincerity.png" ) ).getImage() );

		tabs = new LazyTabbedPane();
		toolbar = new JPanel( new FlowLayout( FlowLayout.LEADING, 10, 0 ) );

		JPanel top = new JPanel();
		top.setBorder( BorderFactory.createEmptyBorder( 10, 0, 10, 0 ) );
		top.setLayout( new BoxLayout( top, BoxLayout.PAGE_AXIS ) );
		top.add( new ContainerPane( sincerity ) );
		top.add( Box.createRigidArea( new Dimension( 0, 10 ) ) );
		top.add( toolbar );

		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( top, BorderLayout.NORTH );
		getContentPane().add( tabs, BorderLayout.CENTER );
	}

	//
	// Attributes
	//

	public JTabbedPane getTabs()
	{
		return tabs;
	}

	public JPanel getToolbar()
	{
		return toolbar;
	}

	//
	// Operations
	//

	public void toConsole()
	{
		dispose();

		try
		{
			new Console( sincerity ).run();
		}
		catch( SincerityException x )
		{
			GuiUtil.error( x );
		}
	}

	//
	// Runnable
	//

	public void run()
	{
		GuiUtil.center( this, .5 );
		setVisible( true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Sincerity sincerity;

	private final JTabbedPane tabs;

	private final JPanel toolbar;
}

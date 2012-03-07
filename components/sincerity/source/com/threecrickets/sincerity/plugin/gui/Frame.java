package com.threecrickets.sincerity.plugin.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;

public class Frame extends JFrame implements Runnable
{
	//
	// Construction
	//

	public Frame( Sincerity sincerity ) throws SincerityException
	{
		super( "Sincerity" );

		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		setIconImage( new ImageIcon( Frame.class.getResource( "sincerity.png" ) ).getImage() );

		pane = new JTabbedPane();
		getContentPane().setLayout( new BorderLayout() );
		getContentPane().add( new ContainerPane( sincerity ), BorderLayout.NORTH );
		getContentPane().add( pane, BorderLayout.CENTER );
	}

	//
	// Attributes
	//

	public JTabbedPane getPane()
	{
		return pane;
	}

	//
	// Operations
	//

	public void run()
	{
		Rectangle bounds = getGraphicsConfiguration().getBounds();
		int width = bounds.width / 2;
		int height = bounds.height / 2;
		int centerX = bounds.x + bounds.width / 2;
		int centerY = bounds.y + bounds.height / 2;
		setLocation( centerX - width / 2, centerY - height / 2 );
		setPreferredSize( new Dimension( width, height ) );
		pack();
		setVisible( true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final JTabbedPane pane;

	private static final long serialVersionUID = 1L;
}

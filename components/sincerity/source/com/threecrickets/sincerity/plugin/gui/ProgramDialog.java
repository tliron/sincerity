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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;

/**
 * Allows starting of a program. When the service plugin is present, also allows
 * servicing the program.
 * 
 * @author Tal Liron
 * @see ProgramsPane
 */
public class ProgramDialog extends JDialog implements ActionListener
{
	//
	// Construction
	//

	public ProgramDialog( Sincerity sincerity, String program ) throws SincerityException
	{
		super( sincerity.getFrame(), program, true );

		this.sincerity = sincerity;
		this.program = program;

		setDefaultCloseOperation( DISPOSE_ON_CLOSE );

		// Content
		JPanel content = new JPanel();
		setContentPane( content );
		content.setLayout( new BoxLayout( content, BoxLayout.PAGE_AXIS ) );
		content.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		// Program arguments
		arguments = new JTextField();
		JLabel argumentsLabel = new JLabel( "Enter the program arguments:", SwingConstants.LEADING );
		argumentsLabel.setLabelFor( arguments );
		arguments.setAlignmentX( 0 );

		content.add( argumentsLabel );
		content.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		content.add( arguments );
		content.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		JButton button = new JButton( "Start" );
		button.setActionCommand( "delegate:start" );
		button.addActionListener( this );
		content.add( button );
		getRootPane().setDefaultButton( button );

		if( sincerity.getPlugins().get( "service" ) != null )
		{
			// Service buttons
			JPanel buttons = new JPanel();
			buttons.setAlignmentX( 0 );
			buttons.setLayout( new BoxLayout( buttons, BoxLayout.LINE_AXIS ) );
			button = new JButton( "Start" );
			button.setActionCommand( "start" );
			button.addActionListener( this );
			buttons.add( button );
			button = new JButton( "Stop" );
			button.setActionCommand( "stop" );
			button.addActionListener( this );
			buttons.add( button );
			button = new JButton( "Restart" );
			button.setActionCommand( "restart" );
			button.addActionListener( this );
			buttons.add( button );
			button = new JButton( "Status" );
			button.setActionCommand( "status" );
			button.addActionListener( this );
			buttons.add( button );

			content.add( Box.createRigidArea( new Dimension( 0, 10 ) ) );
			content.add( new JSeparator( SwingConstants.HORIZONTAL ) );
			content.add( Box.createRigidArea( new Dimension( 0, 10 ) ) );
			content.add( new JLabel( "Or, to run this program as a background service:" ) );
			content.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
			content.add( buttons );
		}

		pack();
		GuiUtil.center( this );
		setVisible( true );
	}

	//
	// ActionListener
	//

	public void actionPerformed( ActionEvent event )
	{
		try
		{
			String command = event.getActionCommand();
			ArrayList<String> argumentList = new ArrayList<String>();
			if( "delegate:start".equals( command ) )
			{
				argumentList.add( command );
				argumentList.add( program );
				argumentList.addAll( Arrays.asList( arguments.getText().split( " " ) ) );
			}
			else
			{
				argumentList.add( "service:service" );
				argumentList.add( program );
				argumentList.add( command );
			}

			String[] argumentArray = argumentList.toArray( new String[argumentList.size()] );

			dispose();
			sincerity.getFrame().toConsole();
			Sincerity.main( argumentArray );
		}
		catch( Exception x )
		{
			GuiUtil.error( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Sincerity sincerity;

	private final String program;

	private final JTextField arguments;
}

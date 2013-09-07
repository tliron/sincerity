/**
 * Copyright 2011-2013 Three Crickets LLC.
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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Reader;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;

/**
 * Shows the Sincerity output and error in a Swing frame.
 * <p>
 * <a
 * href="http://exampledepot.com/egs/javax.swing.text/ta_Console.html">Source<
 * /a>
 * 
 * @author Tal Liron
 * @see Sincerity#getOut()
 * @see Sincerity#getErr()
 */
public class Console extends JFrame implements Runnable, ActionListener
{
	//
	// Constants
	//

	public static final String CONSOLE_ATTRIBUTE = "com.threecrickets.sincerity.console";

	//
	// Static attributes
	//

	public static Console getCurrent()
	{
		return (Console) Bootstrap.getAttributes().get( CONSOLE_ATTRIBUTE );
	}

	//
	// Construction
	//

	public Console( final Sincerity sincerity ) throws SincerityException
	{
		super( "Sincerity" );

		this.sincerity = sincerity;

		setDefaultCloseOperation( DISPOSE_ON_CLOSE );
		setIconImage( new ImageIcon( Frame.class.getResource( "sincerity.png" ) ).getImage() );

		// Label
		JLabel label = new JLabel( "Command output:" );
		label.setBorder( BorderFactory.createEmptyBorder( 5, 5, 0, 5 ) );
		getContentPane().add( label, BorderLayout.PAGE_START );

		// Text area
		textArea = new JTextArea();
		textArea.setFont( new Font( "monospaced", Font.PLAIN, textArea.getFont().getSize() ) );
		textArea.setEditable( false );
		textArea.setRows( 30 );
		textArea.setColumns( 80 );
		JScrollPane textPane = new JScrollPane( textArea );
		textPane.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		getContentPane().add( textPane, BorderLayout.CENTER );

		// Buttons
		JButton okButton = new JButton( "OK" );
		okButton.addActionListener( this );
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout( new BoxLayout( buttonPane, BoxLayout.LINE_AXIS ) );
		buttonPane.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
		buttonPane.add( Box.createHorizontalGlue() );
		buttonPane.add( okButton );
		getContentPane().add( buttonPane, BorderLayout.PAGE_END );
		getRootPane().setDefaultButton( okButton );

		capture( out );
		capture( err );

		oldOut = sincerity.getOut();
		oldErr = sincerity.getErr();

		sincerity.setOut( out );
		sincerity.setErr( err );
	}

	//
	// Attributes
	//

	public final PipedWriter out = new PipedWriter();

	public final PipedWriter err = new PipedWriter();

	//
	// Runnable
	//

	public void run()
	{
		Bootstrap.getAttributes().put( CONSOLE_ATTRIBUTE, this );
		GuiUtil.center( this );
		setVisible( true );
	}

	//
	// ActionListener
	//

	public void actionPerformed( ActionEvent event )
	{
		dispose();
	}

	//
	// Window
	//

	@Override
	public void dispose()
	{
		sincerity.setOut( oldOut );
		sincerity.setErr( oldErr );

		try
		{
			out.close();
			err.close();
		}
		catch( IOException x )
		{
			GuiUtil.error( x );
		}

		Bootstrap.getAttributes().remove( CONSOLE_ATTRIBUTE );
		super.dispose();

		Sincerity.main( new String[]
		{
			"gui" + Command.PLUGIN_COMMAND_SEPARATOR + "gui"
		} );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Sincerity sincerity;

	private final PrintWriter oldOut;

	private final PrintWriter oldErr;

	private final JTextArea textArea;

	public void capture( PipedWriter out ) throws SincerityException
	{
		try
		{
			new Thread( new Pipe( new PipedReader( out ) ) ).start();
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not initialize GUI console", x );
		}
	}

	private class Add implements Runnable
	{
		public Add( String string )
		{
			this.string = string;
		}

		public void run()
		{
			textArea.append( string );
			textArea.setCaretPosition( textArea.getDocument().getLength() );
		}

		private final String string;
	}

	private class Pipe implements Runnable
	{
		public Pipe( Reader reader )
		{
			this.reader = reader;
		}

		public void run()
		{
			char[] buffer = new char[1024];
			try
			{
				while( true )
				{
					int len = reader.read( buffer );
					if( len == -1 )
						break;

					SwingUtilities.invokeLater( new Add( new String( buffer, 0, len ) ) );
				}
			}
			catch( IOException x )
			{
			}
		}

		private final Reader reader;
	}
}
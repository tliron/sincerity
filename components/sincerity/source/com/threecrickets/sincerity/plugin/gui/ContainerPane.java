package com.threecrickets.sincerity.plugin.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;

public class ContainerPane extends JPanel implements ActionListener
{
	//
	// Construction
	//

	public ContainerPane( Sincerity sincerity ) throws SincerityException
	{
		super( new FlowLayout( FlowLayout.LEADING, 10, 10 ) );

		this.sincerity = sincerity;

		JLabel label = new JLabel();
		label.setHorizontalAlignment( SwingConstants.LEADING );
		add( label );

		try
		{
			Container container = sincerity.getContainer();
			label.setText( "Using container at: " + container.getRoot() );
		}
		catch( NoContainerException x )
		{
			label.setText( "You are not using a container. Do you want to create one?" );
			JButton create = new JButton( "Create" );
			create.setActionCommand( "create" );
			create.addActionListener( this );
			add( create );
		}
	}

	//
	// ActionListener
	//

	public void actionPerformed( ActionEvent event )
	{
		if( "create".equals( event.getActionCommand() ) )
		{
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
			chooser.setDialogTitle( "Container root" );
			if( chooser.showDialog( this, "Create this directory" ) == JFileChooser.APPROVE_OPTION )
			{
				File containerRoot = chooser.getSelectedFile();
				sincerity.getFrame().dispose();
				Sincerity.main( new String[]
				{
					"container" + Command.PLUGIN_COMMAND_SEPARATOR + "create", containerRoot.toString(), Command.COMMANDS_SEPARATOR, "gui" + Command.PLUGIN_COMMAND_SEPARATOR + "gui"
				} );
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Sincerity sincerity;
}

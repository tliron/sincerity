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

package com.threecrickets.sincerity.plugin.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.Template;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;

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
			JButton add = new JButton( "Add/Install" );
			add.setActionCommand( "add" );
			add.addActionListener( this );
			add( add );
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
		try
		{
			String command = event.getActionCommand();
			if( "create".equals( command ) )
			{
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				chooser.setDialogTitle( "Container root" );
				if( chooser.showDialog( this, "Create this folder" ) == JFileChooser.APPROVE_OPTION )
				{
					File containerRoot = chooser.getSelectedFile();

					boolean force = false;
					if( containerRoot.isDirectory() )
					{
						if( new File( containerRoot, Container.SINCERITY_DIR ).isDirectory() )
						{
							if( JOptionPane.showConfirmDialog( this, "The folder is already a container. Do you want to use it?", "Container root", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION )
							{
								sincerity.getFrame().dispose();
								Sincerity.main( new String[]
								{
									"container" + Command.PLUGIN_COMMAND_SEPARATOR + "use", containerRoot.toString(), Command.COMMANDS_SEPARATOR, "gui" + Command.PLUGIN_COMMAND_SEPARATOR + "gui"
								} );
							}
							return;
						}
						else
						{
							if( JOptionPane.showConfirmDialog( this, "The folder already exists. Do you want to to turn it into a container?", "Container root", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION )
								force = true;
							else
								return;
						}
					}

					List<Template> templates = sincerity.getTemplates();
					Template defaultTemplate = null;
					for( Template template : templates )
					{
						if( "default".equals( template.root.getName() ) )
						{
							defaultTemplate = template;
							break;
						}
					}
					Template[] templatesArray = templates.toArray( new Template[templates.size()] );
					Template template = (Template) JOptionPane.showInputDialog( this, "Choose from available templates:", "Container template", JOptionPane.PLAIN_MESSAGE, null, templatesArray, defaultTemplate );
					if( template == null )
						return;

					sincerity.getFrame().dispose();
					if( force )
						Sincerity.main( new String[]
						{
							"container" + Command.PLUGIN_COMMAND_SEPARATOR + "create", containerRoot.toString(), template.getName(), "--force", Command.COMMANDS_SEPARATOR,
							"gui" + Command.PLUGIN_COMMAND_SEPARATOR + "gui"
						} );
					else
						Sincerity.main( new String[]
						{
							"container" + Command.PLUGIN_COMMAND_SEPARATOR + "create", containerRoot.toString(), template.getName(), Command.COMMANDS_SEPARATOR, "gui" + Command.PLUGIN_COMMAND_SEPARATOR + "gui"
						} );
				}
			}
			else if( "add".equals( command ) )
			{
				new AddDialog( sincerity );
			}
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

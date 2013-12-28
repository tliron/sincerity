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
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.gui.internal.CheckBoxList;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;

/**
 * Shows a list of available "add"-type shortcuts available in the current
 * container, allowing the user to add a selection of them and install them.
 * 
 * @author Tal Liron
 * @see AddDependenciesButton
 */
public class AddDependenciesDialog extends JDialog implements ActionListener
{
	//
	// Construction
	//

	public AddDependenciesDialog( Sincerity sincerity ) throws SincerityException
	{
		super( sincerity.getFrame() );

		this.sincerity = sincerity;

		setTitle( "Add and Install" );

		// See:
		// http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/uiswing/examples/components/ListDialogRunnerProject/src/components/ListDialog.java

		// Buttons
		JButton cancelButton = new JButton( "Cancel" );
		cancelButton.addActionListener( this );
		JButton addButton = new JButton( "Add and install" );
		addButton.setActionCommand( "install" );
		addButton.addActionListener( this );
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout( new BoxLayout( buttonPane, BoxLayout.LINE_AXIS ) );
		buttonPane.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
		buttonPane.add( Box.createHorizontalGlue() );
		buttonPane.add( cancelButton );
		buttonPane.add( Box.createRigidArea( new Dimension( 10, 0 ) ) );
		buttonPane.add( addButton );
		getRootPane().setDefaultButton( addButton );

		// Shortcuts
		List<String> shortcuts = sincerity.getContainer().getShortcuts().getByType( "add" );
		Collections.sort( shortcuts );
		JCheckBox[] shortcutsArray = new JCheckBox[shortcuts.size()];
		int i = 0;
		for( String shortcut : shortcuts )
			shortcutsArray[i++] = new JCheckBox( shortcut.substring( shortcut.indexOf( Shortcuts.SHORTCUT_TYPE_SEPARATOR ) + Shortcuts.SHORTCUT_TYPE_SEPARATOR.length() ) );

		// List
		list = new CheckBoxList( shortcutsArray );
		list.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
		list.setLayoutOrientation( JList.HORIZONTAL_WRAP );
		list.setVisibleRowCount( -1 );

		// List scroller
		JScrollPane listScroller = new JScrollPane( list );
		listScroller.setPreferredSize( new Dimension( 400, 200 ) );
		listScroller.setAlignmentX( LEFT_ALIGNMENT );

		// List pane
		JPanel listPane = new JPanel();
		listPane.setLayout( new BoxLayout( listPane, BoxLayout.PAGE_AXIS ) );
		JLabel label = new JLabel( "Choose shorcuts:" );
		label.setLabelFor( list );
		listPane.add( label );
		listPane.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		listPane.add( listScroller );
		listPane.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

		// Assemble
		java.awt.Container contentPane = getContentPane();
		contentPane.add( listPane, BorderLayout.CENTER );
		contentPane.add( buttonPane, BorderLayout.PAGE_END );

		GuiUtil.center( this );
		setVisible( true );
	}

	//
	// ActionListener
	//

	public void actionPerformed( ActionEvent event )
	{
		if( "install".equals( event.getActionCommand() ) )
		{
			ArrayList<String> shortcuts = new ArrayList<String>();
			int size = list.getModel().getSize();
			for( int i = 0; i < size; i++ )
			{
				JCheckBox checkBox = (JCheckBox) list.getModel().getElementAt( i );
				if( checkBox.isSelected() )
					shortcuts.add( checkBox.getText() );
			}

			if( !shortcuts.isEmpty() )
			{
				try
				{
					for( String shortcut : shortcuts )
						sincerity.run( "dependencies" + Command.PLUGIN_COMMAND_SEPARATOR + "add", shortcut );

					sincerity.getFrame().toConsole();
					Sincerity.main( new String[]
					{
						"dependencies" + Command.PLUGIN_COMMAND_SEPARATOR + "install", Command.COMMANDS_SEPARATOR, "gui" + Command.PLUGIN_COMMAND_SEPARATOR + "gui"
					} );
				}
				catch( Exception x )
				{
					GuiUtil.error( x );
					return;
				}
			}
		}

		dispose();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Sincerity sincerity;

	private final CheckBoxList list;
}

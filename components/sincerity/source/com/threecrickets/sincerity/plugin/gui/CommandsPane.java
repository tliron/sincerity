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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.HelpPlugin;
import com.threecrickets.sincerity.plugin.gui.internal.EnhancedNode;
import com.threecrickets.sincerity.plugin.gui.internal.EnhancedNodeListener;
import com.threecrickets.sincerity.plugin.gui.internal.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;
import com.threecrickets.sincerity.plugin.gui.internal.SortedNode;
import com.threecrickets.sincerity.plugin.gui.internal.WrappedText;

/**
 * Shows all commands available in all plugins installed in the current
 * container or the Sincerity installation if no container is in use.
 * 
 * @author Tal Liron
 * @see HelpPlugin
 */
public class CommandsPane extends JPanel implements ItemListener, EnhancedNodeListener
{
	//
	// Construction
	//

	public CommandsPane( Sincerity sincerity ) throws SincerityException
	{
		super( new BorderLayout() );

		this.sincerity = sincerity;

		tree = new JTree();
		tree.setCellRenderer( new EnhancedTreeCellRenderer() );
		tree.setRootVisible( false );
		EnhancedNode.addListener( tree, this );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		groupByPluginCheckBox = new JCheckBox( "Group by plugin", groupByPlugin );
		groupByPluginCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		buttons.add( groupByPluginCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( new WrappedText( "Double click a command to run it." ) );

		add( buttons, BorderLayout.EAST );

		refresh();
	}

	//
	// ItemListener
	//

	public void itemStateChanged( ItemEvent event )
	{
		ItemSelectable item = event.getItemSelectable();
		boolean selected = event.getStateChange() == ItemEvent.SELECTED;
		if( item == groupByPluginCheckBox )
			groupByPlugin = selected;
		refresh();
	}

	//
	// EnhancedNodeListener
	//

	public void nodeActivated( Object value )
	{
		if( value instanceof String )
		{
			String arguments = JOptionPane.showInputDialog( this, "Enter the command arguments:", (String) value, JOptionPane.INFORMATION_MESSAGE );
			if( arguments != null )
			{
				ArrayList<String> argumentList = new ArrayList<String>( Arrays.asList( arguments ) );
				argumentList.add( 0, (String) value );
				// argumentList.add( Command.COMMANDS_SEPARATOR );
				// argumentList.add( "gui" + Command.PLUGIN_COMMAND_SEPARATOR +
				// "gui" );
				String[] argumentArray = argumentList.toArray( new String[argumentList.size()] );

				sincerity.getFrame().toConsole();
				try
				{
					Sincerity.main( argumentArray );
				}
				catch( Exception x )
				{
					GuiUtil.error( x );
				}
			}
		}
	}

	//
	// Operations
	//

	public void refresh()
	{
		try
		{
			SortedNode root = new SortedNode();
			if( groupByPlugin )
			{
				for( Plugin1 plugin : sincerity.getPlugins().values() )
					root.add( GuiUtil.createPluginNode( plugin, true ) );
			}
			else
			{
				for( Plugin1 plugin : sincerity.getPlugins().values() )
					for( String command : plugin.getCommands() )
						root.add( GuiUtil.createCommandNode( command, plugin, true ) );
			}

			tree.setModel( new DefaultTreeModel( root ) );
			GuiUtil.expandTree( tree, true );
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

	private final JTree tree;

	private final JCheckBox groupByPluginCheckBox;

	private boolean groupByPlugin = true;
}

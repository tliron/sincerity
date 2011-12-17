package com.threecrickets.sincerity.plugin.gui;

import java.awt.BorderLayout;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;

public class ActionsPane extends JPanel implements ItemListener
{
	//
	// Construction
	//

	public ActionsPane( Sincerity sincerity ) throws SincerityException
	{
		super( new BorderLayout() );

		this.sincerity = sincerity;

		tree = new JTree();
		tree.setCellRenderer( new EnhancedTreeCellRenderer() );
		tree.setRootVisible( false );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		groupByPluginCheckBox = new JCheckBox( "Group by plugin", groupByPlugin );
		groupByPluginCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		buttons.add( groupByPluginCheckBox );

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
	// Operations
	//

	public void refresh()
	{
		try
		{
			DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			if( groupByPlugin )
			{
				for( Plugin plugin : sincerity.getPlugins().values() )
					root.add( GuiUtil.createPluginNode( plugin, true ) );
			}
			else
			{
				for( Plugin plugin : sincerity.getPlugins().values() )
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

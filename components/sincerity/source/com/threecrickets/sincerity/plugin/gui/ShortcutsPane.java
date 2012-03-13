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

import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;

public class ShortcutsPane extends JPanel implements ItemListener
{
	//
	// Construction
	//

	public ShortcutsPane( Sincerity sincerity ) throws SincerityException
	{
		super( new BorderLayout() );

		sincerity.getContainer();

		this.sincerity = sincerity;

		tree = new JTree();
		tree.setCellRenderer( new EnhancedTreeCellRenderer() );
		tree.setRootVisible( false );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		groupByTypeCheckBox = new JCheckBox( "Group by type", groupByType );
		groupByTypeCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		buttons.add( groupByTypeCheckBox );

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
		if( item == groupByTypeCheckBox )
			groupByType = selected;
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
			if( groupByType )
			{
				Shortcuts shortcuts = sincerity.getContainer().getShortcuts();
				root.add( GuiUtil.createShortcutTypeNode( "add", shortcuts ) );
				root.add( GuiUtil.createShortcutTypeNode( "attach", shortcuts ) );
			}
			else
			{
				Shortcuts shortcuts = sincerity.getContainer().getShortcuts();
				for( String shortcut : shortcuts )
					root.add( GuiUtil.createShortcutNode( shortcut, true ) );
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

	private final JCheckBox groupByTypeCheckBox;

	private boolean groupByType = true;
}

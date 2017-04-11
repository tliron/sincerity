/**
 * Copyright 2011-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin.swing;

import java.awt.BorderLayout;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.ShortcutsPlugin;
import com.threecrickets.sincerity.util.swing.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.util.swing.PleaseWaitTreeModel;
import com.threecrickets.sincerity.util.swing.SortedNode;

/**
 * Shows all shortcuts available in the current container.
 * 
 * @author Tal Liron
 * @see ShortcutsPlugin
 */
public class ShortcutsPane extends JPanel implements Refreshable, ItemListener
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
		tree.setModel( new PleaseWaitTreeModel() );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		groupByTypeCheckBox = new JCheckBox( "Group by type", groupByType );
		groupByTypeCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		buttons.add( groupByTypeCheckBox );

		add( buttons, BorderLayout.EAST );
	}

	//
	// Refreshable
	//

	public void refresh()
	{
		try
		{
			SortedNode root = new SortedNode();
			if( groupByType )
			{
				Shortcuts shortcuts = sincerity.getContainer().getShortcuts();
				root.add( SwingUtil.createShortcutTypeNode( "add", shortcuts ) );
				root.add( SwingUtil.createShortcutTypeNode( "attach", shortcuts ) );
			}
			else
			{
				Shortcuts shortcuts = sincerity.getContainer().getShortcuts();
				for( String shortcut : shortcuts )
					root.add( SwingUtil.createShortcutNode( shortcut, true ) );
			}

			tree.setModel( new DefaultTreeModel( root ) );
			SwingUtil.expandTree( tree, true );
		}
		catch( SincerityException x )
		{
			SwingUtil.error( x );
		}
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Sincerity sincerity;

	private final JTree tree;

	private final JCheckBox groupByTypeCheckBox;

	private boolean groupByType = true;
}

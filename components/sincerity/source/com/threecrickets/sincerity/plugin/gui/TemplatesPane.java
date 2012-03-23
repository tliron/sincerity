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

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.Template;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.TemplatesPlugin;
import com.threecrickets.sincerity.plugin.gui.internal.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;
import com.threecrickets.sincerity.plugin.gui.internal.SortedNode;

/**
 * Shows all templates available in the Sincerity installation.
 * 
 * @author Tal Liron
 * @see TemplatesPlugin
 */
public class TemplatesPane extends JPanel
{
	//
	// Construction
	//

	public TemplatesPane( Sincerity sincerity ) throws SincerityException
	{
		super( new BorderLayout() );

		this.sincerity = sincerity;

		tree = new JTree();
		tree.setCellRenderer( new EnhancedTreeCellRenderer() );
		tree.setRootVisible( false );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );

		add( buttons, BorderLayout.EAST );

		refresh();
	}

	//
	// Operations
	//

	public void refresh()
	{
		try
		{
			SortedNode root = new SortedNode();

			for( Template template : sincerity.getTemplates() )
				root.add( GuiUtil.createTemplateNode( template ) );

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
}

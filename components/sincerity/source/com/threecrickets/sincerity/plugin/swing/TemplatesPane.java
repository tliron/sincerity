/**
 * Copyright 2011-2016 Three Crickets LLC.
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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.Template;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.TemplatesPlugin;
import com.threecrickets.sincerity.util.swing.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.util.swing.PleaseWaitTreeModel;
import com.threecrickets.sincerity.util.swing.SortedNode;

/**
 * Shows all templates available in the Sincerity installation.
 * 
 * @author Tal Liron
 * @see TemplatesPlugin
 */
public class TemplatesPane extends JPanel implements Refreshable
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
		tree.setModel( new PleaseWaitTreeModel() );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );
	}

	//
	// Refreshable
	//

	public void refresh()
	{
		try
		{
			SortedNode root = new SortedNode();

			for( Template template : sincerity.getTemplates() )
				root.add( SwingUtil.createTemplateNode( template ) );

			tree.setModel( new DefaultTreeModel( root ) );
			SwingUtil.expandTree( tree, true );
		}
		catch( SincerityException x )
		{
			SwingUtil.error( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Sincerity sincerity;

	private final JTree tree;
}

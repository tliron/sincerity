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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.DelegatePlugin;
import com.threecrickets.sincerity.plugin.gui.internal.EnhancedNode;
import com.threecrickets.sincerity.plugin.gui.internal.EnhancedNodeListener;
import com.threecrickets.sincerity.plugin.gui.internal.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;
import com.threecrickets.sincerity.plugin.gui.internal.PleaseWaitTreeModel;
import com.threecrickets.sincerity.plugin.gui.internal.SortedNode;

/**
 * Shows all programs available in the Sincerity installation.
 * 
 * @author Tal Liron
 * @see DelegatePlugin
 * @see ProgramDialog
 */
public class ProgramsPane extends JPanel implements Refreshable, EnhancedNodeListener
{
	//
	// Construction
	//

	public ProgramsPane( Sincerity sincerity ) throws SincerityException
	{
		super( new BorderLayout() );

		sincerity.getContainer();

		this.sincerity = sincerity;

		tree = new JTree();
		tree.setCellRenderer( new EnhancedTreeCellRenderer() );
		tree.setRootVisible( false );
		tree.setModel( new PleaseWaitTreeModel() );
		EnhancedNode.addListener( tree, this );

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

			for( String program : sincerity.getContainer().getPrograms() )
				root.add( GuiUtil.createProgramNode( program ) );

			tree.setModel( new DefaultTreeModel( root ) );
			GuiUtil.expandTree( tree, true );
		}
		catch( SincerityException x )
		{
			GuiUtil.error( x );
		}
	}

	//
	// EnhancedNodeListener
	//

	public void nodeActivated( Object value )
	{
		try
		{
			new ProgramDialog( sincerity, (String) value );
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

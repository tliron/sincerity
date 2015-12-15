/**
 * Copyright 2011-2015 Three Crickets LLC.
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

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.dependencies.Repositories;
import com.threecrickets.sincerity.dependencies.Repository;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.RepositoriesPlugin;
import com.threecrickets.sincerity.util.swing.EnhancedNode;
import com.threecrickets.sincerity.util.swing.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.util.swing.PleaseWaitTreeModel;
import com.threecrickets.sincerity.util.swing.SortedNode;

/**
 * Shows all repositories attached to the current container.
 * 
 * @author Tal Liron
 * @see RepositoriesPlugin
 */
public class RepositoriesPane extends JPanel implements Refreshable
{
	//
	// Construction
	//

	public RepositoriesPane( Sincerity sincerity ) throws SincerityException
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

		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );

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

			EnhancedNode publicRoot = new EnhancedNode( null, "Public", SwingUtil.FOLDER_ICON );
			EnhancedNode privateRoot = new EnhancedNode( null, "Private", SwingUtil.FOLDER_ICON );

			Container<?, ?> container = sincerity.getContainer();
			Repositories repositories = container.getRepositories();
			for( Repository repository : repositories.get( "public" ) )
				publicRoot.add( SwingUtil.createRepositoryNode( repository ) );
			for( Repository repository : repositories.get( "private" ) )
				privateRoot.add( SwingUtil.createRepositoryNode( repository ) );

			if( publicRoot.getChildCount() > 0 )
				root.add( publicRoot );
			if( privateRoot.getChildCount() > 0 )
				root.add( privateRoot );

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

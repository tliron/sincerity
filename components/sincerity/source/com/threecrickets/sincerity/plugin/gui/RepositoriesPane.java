package com.threecrickets.sincerity.plugin.gui;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.ivy.plugins.resolver.DependencyResolver;

import com.threecrickets.sincerity.Repositories;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;

public class RepositoriesPane extends JPanel
{
	//
	// Construction
	//

	public RepositoriesPane( Sincerity sincerity ) throws SincerityException
	{
		super( new BorderLayout() );

		this.sincerity = sincerity;

		sincerity.getContainer();

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
			DefaultMutableTreeNode root = new DefaultMutableTreeNode();

			DefaultMutableTreeNode publicRoot = new EnhancedNode( null, "Public", GuiUtil.FOLDER_ICON );
			DefaultMutableTreeNode privateRoot = new EnhancedNode( null, "Private", GuiUtil.FOLDER_ICON );

			Repositories repositories = sincerity.getContainer().getRepositories();
			for( DependencyResolver resolver : repositories.getResolvers( "public" ) )
				publicRoot.add( GuiUtil.createRepositoryNode( resolver ) );
			for( DependencyResolver resolver : repositories.getResolvers( "private" ) )
				privateRoot.add( GuiUtil.createRepositoryNode( resolver ) );

			if( publicRoot.getChildCount() > 0 )
				root.add( publicRoot );
			if( privateRoot.getChildCount() > 0 )
				root.add( privateRoot );

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

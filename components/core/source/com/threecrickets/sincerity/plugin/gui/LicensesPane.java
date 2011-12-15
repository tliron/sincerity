package com.threecrickets.sincerity.plugin.gui;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.ivy.core.module.descriptor.License;

import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.exception.SincerityException;

public class LicensesPane extends JPanel
{
	public LicensesPane( Dependencies dependencies ) throws SincerityException
	{
		super( new GridLayout( 1, 1 ) );

		DefaultMutableTreeNode root = new DefaultMutableTreeNode( "Root" );
		for( License license : dependencies.getResolvedDependencies().getLicenses() )
			GuiUtil.addLicense( license, root );

		JTree tree = new JTree( root );
		GuiUtil.expandTree( tree, true );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree );

		tree.setRootVisible( false );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

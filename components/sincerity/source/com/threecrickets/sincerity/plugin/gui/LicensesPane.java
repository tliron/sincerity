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

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import org.apache.ivy.core.module.descriptor.License;

import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.ResolvedDependencies;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.ContainerPlugin;
import com.threecrickets.sincerity.plugin.gui.internal.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;
import com.threecrickets.sincerity.plugin.gui.internal.SortedNode;

/**
 * Shows all licenses used by all <i>resolved</i> dependencies installed in the
 * current container.
 * 
 * @author Tal Liron
 * @see ContainerPlugin
 * @see ResolvedDependencies
 */
public class LicensesPane extends JPanel implements ItemListener
{
	//
	// Construction
	//

	public LicensesPane( Dependencies dependencies ) throws SincerityException
	{
		super( new BorderLayout() );

		this.dependencies = dependencies;

		tree = new JTree();
		tree.setCellRenderer( new EnhancedTreeCellRenderer() );
		tree.setRootVisible( false );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		showDependenciesCheckBox = new JCheckBox( "Show dependencies", showDependencies );
		showDependenciesCheckBox.addItemListener( this );
		showArtifactsCheckBox = new JCheckBox( "Show artifacts", showArtifacts );
		showArtifactsCheckBox.addItemListener( this );
		showPackageContentsCheckBox = new JCheckBox( "Show package contents", showPackageContents );
		showPackageContentsCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		buttons.add( showDependenciesCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( showArtifactsCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( showPackageContentsCheckBox );

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
		if( item == showDependenciesCheckBox )
			showDependencies = selected;
		else if( item == showArtifactsCheckBox )
		{
			showArtifacts = selected;
			showPackageContentsCheckBox.setEnabled( showArtifacts );
		}
		else if( item == showPackageContentsCheckBox )
			showPackageContents = selected;
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

			for( License license : dependencies.getResolvedDependencies().getLicenses() )
				root.add( GuiUtil.createLicenseNode( license, dependencies, true, showDependencies, showArtifacts, showPackageContents ) );

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

	private final Dependencies dependencies;

	private final JTree tree;

	private final JCheckBox showDependenciesCheckBox;

	private final JCheckBox showArtifactsCheckBox;

	private final JCheckBox showPackageContentsCheckBox;

	private boolean showDependencies = true;

	private boolean showArtifacts = false;

	private boolean showPackageContents = false;
}

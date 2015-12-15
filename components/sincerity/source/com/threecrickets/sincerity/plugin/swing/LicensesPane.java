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
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.plaf.TreeUI;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.dependencies.License;
import com.threecrickets.sincerity.dependencies.Modules;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.ContainerPlugin;
import com.threecrickets.sincerity.util.swing.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.util.swing.PleaseWaitTreeModel;
import com.threecrickets.sincerity.util.swing.SortedNode;
import com.threecrickets.sincerity.util.swing.WrappedText;

/**
 * Shows all licenses used by all <i>resolved</i> dependencies installed in the
 * current container.
 * 
 * @author Tal Liron
 * @see ContainerPlugin
 * @see Modules
 */
public class LicensesPane extends JPanel implements Refreshable, ItemListener
{
	//
	// Construction
	//

	public LicensesPane( Dependencies<?> dependencies ) throws SincerityException
	{
		super( new BorderLayout() );

		this.dependencies = dependencies;

		tree = new JTree();
		tree.setLargeModel( true );
		tree.setCellRenderer( new EnhancedTreeCellRenderer() );
		tree.setRootVisible( false );
		tree.setModel( new PleaseWaitTreeModel() );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		showDependenciesCheckBox = new JCheckBox( "Show dependencies", showDependencies );
		showDependenciesCheckBox.addItemListener( this );
		showArtifactsCheckBox = new JCheckBox( "Show artifacts", showArtifacts );
		showArtifactsCheckBox.addItemListener( this );
		showPackageContentsCheckBox = new JCheckBox( "Show package contents", showPackageContents );
		showPackageContentsCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		buttons.add( showDependenciesCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( showArtifactsCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( showPackageContentsCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( new JSeparator( SwingConstants.HORIZONTAL ) );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( new WrappedText( "Note that this list only reflects dependencies that have already been installed." ) );

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

			for( License license : dependencies.getModules().getLicenses() )
				root.add( SwingUtil.createLicenseNode( license, dependencies, true, showDependencies, showArtifacts, showPackageContents ) );

			TreeUI ui = tree.getUI();
			tree.setUI( null );
			tree.setModel( new DefaultTreeModel( root ) );
			SwingUtil.expandTree( tree, true );
			tree.setUI( ui );
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
		if( item == showDependenciesCheckBox )
			showDependencies = selected;
		else if( item == showArtifactsCheckBox )
		{
			showArtifacts = selected;
			showPackageContentsCheckBox.setEnabled( showArtifacts );
		}
		else if( item == showPackageContentsCheckBox )
			showPackageContents = selected;

		tree.setModel( new PleaseWaitTreeModel() );
		SwingUtilities.invokeLater( new Runnable()
		{
			public void run()
			{
				refresh();
			}
		} );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Dependencies<?> dependencies;

	private final JTree tree;

	private final JCheckBox showDependenciesCheckBox;

	private final JCheckBox showArtifactsCheckBox;

	private final JCheckBox showPackageContentsCheckBox;

	private boolean showDependencies = true;

	private boolean showArtifacts = false;

	private boolean showPackageContents = false;
}

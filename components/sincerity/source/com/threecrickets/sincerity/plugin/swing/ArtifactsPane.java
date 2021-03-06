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
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

import com.threecrickets.sincerity.dependencies.Artifact;
import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.plugin.ArtifactsPlugin;
import com.threecrickets.sincerity.util.swing.EnhancedTreeCellRenderer;
import com.threecrickets.sincerity.util.swing.PleaseWaitTreeModel;
import com.threecrickets.sincerity.util.swing.SortedNode;
import com.threecrickets.sincerity.util.swing.WrappedText;

/**
 * Shows all artifacts installed in the current container.
 * 
 * @author Tal Liron
 * @see ArtifactsPlugin
 */
public class ArtifactsPane extends JPanel implements Refreshable, ItemListener
{
	//
	// Construction
	//

	public ArtifactsPane( Dependencies<?> dependencies ) throws SincerityException
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

		groupByTypeCheckBox = new JCheckBox( "Group by type", groupByType );
		groupByTypeCheckBox.addItemListener( this );
		showPackageContentsCheckBox = new JCheckBox( "Show package contents", showPackageContents );
		showPackageContentsCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		buttons.add( groupByTypeCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( showPackageContentsCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( new JSeparator( SwingConstants.HORIZONTAL ) );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( new WrappedText( "Note that this list only reflects artifacts that have been installed by Sincerity." ) );

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
				HashMap<String, ArrayList<Artifact>> groups = new HashMap<String, ArrayList<Artifact>>();
				for( Artifact artifact : dependencies.getModules().getArtifacts() )
				{
					String type = artifact.getType();
					ArrayList<Artifact> artifacts = groups.get( type );
					if( artifacts == null )
					{
						artifacts = new ArrayList<Artifact>();
						groups.put( type, artifacts );
					}
					artifacts.add( artifact );
				}

				for( Map.Entry<String, ArrayList<Artifact>> entry : groups.entrySet() )
				{
					SortedNode groupNode = new SortedNode( "<html><b>" + entry.getKey() + "</b></html>" );
					root.add( groupNode );
					for( Artifact artifact : entry.getValue() )
						groupNode.add( SwingUtil.createArtifactNode( artifact, dependencies, showPackageContents ) );
				}
			}
			else
			{
				for( Artifact artifact : dependencies.getModules().getArtifacts() )
					root.add( SwingUtil.createArtifactNode( artifact, dependencies, showPackageContents ) );
			}

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
		if( item == groupByTypeCheckBox )
			groupByType = selected;
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

	private final JCheckBox groupByTypeCheckBox;

	private final JCheckBox showPackageContentsCheckBox;

	private boolean groupByType = true;

	private boolean showPackageContents = false;
}

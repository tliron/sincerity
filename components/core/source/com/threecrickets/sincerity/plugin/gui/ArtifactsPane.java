package com.threecrickets.sincerity.plugin.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.apache.ivy.core.module.descriptor.Artifact;

import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.exception.SincerityException;

public class ArtifactsPane extends JPanel implements ItemListener
{
	//
	// Construction
	//

	public ArtifactsPane( Dependencies dependencies ) throws SincerityException
	{
		super( new BorderLayout() );

		this.dependencies = dependencies;

		tree = new JTree();
		tree.setCellRenderer( new EnhancedTreeCellRenderer() );
		tree.setRootVisible( false );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		groupByTypeCheckBox = new JCheckBox( "Group by type", groupByType );
		groupByTypeCheckBox.addItemListener( this );
		showPackageContentsCheckBox = new JCheckBox( "Show package contents", showPackageContents );
		showPackageContentsCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		buttons.add( groupByTypeCheckBox );
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
		if( item == groupByTypeCheckBox )
			groupByType = selected;
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
			DefaultMutableTreeNode root = new DefaultMutableTreeNode();
			if( groupByType )
			{
				HashMap<String, ArrayList<Artifact>> groups = new HashMap<String, ArrayList<Artifact>>();
				for( Artifact artifact : dependencies.getResolvedDependencies().getArtifacts() )
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
					DefaultMutableTreeNode groupNode = new DefaultMutableTreeNode( "<html><b>" + entry.getKey() + "</b></html>" );
					root.add( groupNode );
					for( Artifact artifact : entry.getValue() )
						groupNode.add( GuiUtil.createArtifactNode( artifact, dependencies, showPackageContents ) );
				}
			}
			else
			{
				for( Artifact artifact : dependencies.getResolvedDependencies().getArtifacts() )
					root.add( GuiUtil.createArtifactNode( artifact, dependencies, showPackageContents ) );
			}

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

	private final JCheckBox groupByTypeCheckBox;

	private final JCheckBox showPackageContentsCheckBox;

	private boolean groupByType = true;

	private boolean showPackageContents = false;
}
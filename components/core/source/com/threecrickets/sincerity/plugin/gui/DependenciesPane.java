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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.ResolvedDependency;
import com.threecrickets.sincerity.exception.SincerityException;

public class DependenciesPane extends JPanel implements ItemListener
{
	public DependenciesPane( Dependencies dependencies ) throws SincerityException
	{
		super( new BorderLayout() );

		this.dependencies = dependencies;

		tree = new JTree();
		tree.setRootVisible( false );

		JScrollPane scrollableTree = new JScrollPane( tree );
		add( scrollableTree, BorderLayout.CENTER );

		includeSubCheckBox = new JCheckBox( "Include sub-dependencies", includeSub );
		includeSubCheckBox.addItemListener( this );
		asTreeCheckBox = new JCheckBox( "Show as tree", asTree );
		asTreeCheckBox.addItemListener( this );
		showArtifactsCheckBox = new JCheckBox( "Show artifacts", showArtifacts );
		showArtifactsCheckBox.addItemListener( this );
		showLicensesCheckBox = new JCheckBox( "Show licenses", showLicenses );
		showLicensesCheckBox.addItemListener( this );

		JPanel buttons = new JPanel();
		buttons.setLayout( new BoxLayout( buttons, BoxLayout.Y_AXIS ) );
		// buttons.add( new JComboBox() );
		// buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( includeSubCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( asTreeCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( showArtifactsCheckBox );
		buttons.add( Box.createRigidArea( new Dimension( 0, 5 ) ) );
		buttons.add( showLicensesCheckBox );

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
		if( item == includeSubCheckBox )
		{
			includeSub = selected;
			asTreeCheckBox.setEnabled( includeSub );
		}
		else if( item == asTreeCheckBox )
			asTree = selected;
		else if( item == showArtifactsCheckBox )
			showArtifacts = selected;
		else if( item == showLicensesCheckBox )
			showLicenses = selected;
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
			for( ResolvedDependency resolvedDependency : includeSub && !asTree ? dependencies.getResolvedDependencies().getAllDependencies() : dependencies.getResolvedDependencies() )
				GuiUtil.addDependency( resolvedDependency, root, dependencies, asTree && includeSub, showLicenses, showArtifacts );

			tree.setModel( new DefaultTreeModel( root ) );
			GuiUtil.expandTree( tree, true );
		}
		catch( SincerityException x )
		{

		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Dependencies dependencies;

	private final JTree tree;

	private final JCheckBox includeSubCheckBox;

	private final JCheckBox asTreeCheckBox;

	private final JCheckBox showArtifactsCheckBox;

	private final JCheckBox showLicensesCheckBox;

	private boolean includeSub = true;

	private boolean asTree = true;

	private boolean showArtifacts = true;

	private boolean showLicenses = true;

	private static final long serialVersionUID = 1L;
}

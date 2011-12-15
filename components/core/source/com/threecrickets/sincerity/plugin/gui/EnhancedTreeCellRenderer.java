package com.threecrickets.sincerity.plugin.gui;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class EnhancedTreeCellRenderer extends DefaultTreeCellRenderer
{
	//
	// DefaultTreeCellRenderer
	//

	@Override
	public Component getTreeCellRendererComponent( JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus )
	{
		super.getTreeCellRendererComponent( tree, value, sel, expanded, leaf, row, hasFocus );
		if( value instanceof EnhancedNode )
		{
			EnhancedNode node = (EnhancedNode) value;
			setIcon( node.getIcon() );
		}
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

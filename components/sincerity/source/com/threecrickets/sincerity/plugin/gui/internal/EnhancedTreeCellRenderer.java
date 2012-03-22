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

package com.threecrickets.sincerity.plugin.gui.internal;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * A Swing tree cell renderer that supports special rendering for
 * {@link EnhancedNode}.
 * 
 * @author Tal Liron
 */
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

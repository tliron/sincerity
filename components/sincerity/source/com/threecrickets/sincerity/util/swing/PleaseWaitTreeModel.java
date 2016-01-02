/**
 * Copyright 2011-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.util.swing;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/**
 * A tree model with a single "please wait" node.
 * 
 * @author Tal Liron
 */
public class PleaseWaitTreeModel extends DefaultTreeModel
{
	//
	// Construction
	//

	public PleaseWaitTreeModel()
	{
		super( new DefaultMutableTreeNode() );
		( (DefaultMutableTreeNode) this.getRoot() ).add( new EnhancedNode( null, "Please wait...", null ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class SortedNode extends DefaultMutableTreeNode
{
	//
	// Construction
	//

	public SortedNode( Object value )
	{
		super( value );
	}

	public SortedNode()
	{
		super();
	}

	//
	// DefaultMutableTreeNode
	//

	@Override
	public void add( MutableTreeNode newChild )
	{
		String string = newChild.toString();
		int i = 0;
		if( children != null )
		{
			for( Object child : children )
			{
				if( string.compareTo( child.toString() ) < 0 )
					break;
				i++;
			}
		}
		insert( newChild, i );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

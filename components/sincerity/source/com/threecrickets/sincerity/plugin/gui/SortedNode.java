package com.threecrickets.sincerity.plugin.gui;

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

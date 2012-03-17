package com.threecrickets.sincerity.plugin.gui;

import java.util.Collections;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class SortedNode extends DefaultMutableTreeNode implements Comparable<SortedNode>
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
	@SuppressWarnings("unchecked")
	public void add( MutableTreeNode newChild )
	{
		super.add( newChild );
		Collections.sort( this.children );
	}

	//
	// Comparable
	//

	public int compareTo( SortedNode o )
	{
		return toString().compareTo( o.toString() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

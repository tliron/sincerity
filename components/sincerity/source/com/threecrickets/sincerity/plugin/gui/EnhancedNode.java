package com.threecrickets.sincerity.plugin.gui;

import javax.swing.ImageIcon;

public class EnhancedNode extends SortedNode
{
	//
	// Construction
	//

	public EnhancedNode( Object value, String string, ImageIcon icon )
	{
		super( value );
		this.string = string;
		this.icon = icon;
	}

	//
	// Attributes
	//

	public ImageIcon getIcon()
	{
		return icon;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return string;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final String string;

	private final ImageIcon icon;
}

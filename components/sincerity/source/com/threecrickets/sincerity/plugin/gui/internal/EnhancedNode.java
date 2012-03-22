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

import javax.swing.ImageIcon;

/**
 * A Swing node which can be configured with separate value, label and icon.
 * 
 * @author Tal Liron
 * @see EnhancedTreeCellRenderer
 */
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

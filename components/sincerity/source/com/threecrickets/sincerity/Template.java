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

package com.threecrickets.sincerity;

import java.io.File;

public class Template
{
	//
	// Construction
	//

	public Template( File root )
	{
		this.root = root;
	}

	//
	// Attributes
	//

	public final File root;

	public String getName()
	{
		return root.getName();
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return getName();
	}
}

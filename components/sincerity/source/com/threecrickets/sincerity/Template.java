/**
 * Copyright 2011-2014 Three Crickets LLC.
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

/**
 * Templates are simple file structures that are copied verbatim when a
 * {@link Container} is created.
 * 
 * @author Tal Liron
 * @see Sincerity#getTemplates()
 */
public class Template
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param root
	 *        The root directory
	 */
	public Template( File root )
	{
		this.root = root;
	}

	//
	// Attributes
	//

	/**
	 * The root directory.
	 */
	public final File root;

	/**
	 * The name (identical the filename of the root directory).
	 * 
	 * @return The name
	 */
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

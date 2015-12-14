/**
 * Copyright 2011-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.packaging;

import java.io.File;
import java.io.PrintWriter;

import com.threecrickets.sincerity.util.RootDirectory;

/**
 * Context used for packaging.
 * 
 * @author Tal Liron
 */
public class PackagingContext extends RootDirectory
{
	/**
	 * Constructor
	 * 
	 * @param root
	 *        The root directory
	 * @param classLoader
	 *        The class loader
	 * @param out
	 *        The print writer
	 * @param verbosity
	 *        The verbosity level
	 */
	public PackagingContext( File root, ClassLoader classLoader, PrintWriter out, int verbosity )
	{
		super( root );
		this.classLoader = classLoader;
		this.out = out;
		this.verbosity = verbosity;
	}

	/**
	 * The class loader.
	 * 
	 * @return The class loader
	 */
	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	/**
	 * The print writer.
	 * 
	 * @return The print writer
	 */
	public PrintWriter getOut()
	{
		return out;
	}

	/**
	 * The verbosity level.
	 * 
	 * @return The verbosity level
	 */
	public int getVerbosity()
	{
		return verbosity;
	}

	private final ClassLoader classLoader;

	private final PrintWriter out;

	private final int verbosity;
}

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

package com.threecrickets.sincerity.util;

import java.io.File;

/**
 * Utilities for files under a root directory.
 */
public class RootDirectory
{
	/**
	 * Constructor.
	 * 
	 * @param root
	 *        The root directory
	 */
	public RootDirectory( File root )
	{
		this.root = root;
	}

	/**
	 * The root directory.
	 * 
	 * @return The root file
	 */
	public File getRoot()
	{
		return root;
	}

	/**
	 * Constructs a path from the root directory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The file
	 */
	public File getFile( String... parts )
	{
		File file = root;
		for( String part : parts )
			file = new File( file, part );
		return file;
	}

	/**
	 * Makes sure that we have an absolute file path, using the root directory
	 * as the start if the supplied path is relative.
	 * 
	 * @param file
	 *        The file
	 * @return The absolute file
	 * @see #getRelativeFile(File)
	 */
	public File getAbsoluteFile( File file )
	{
		if( !file.isAbsolute() )
			return new File( root, file.getPath() );
		else
			return file;
	}

	/**
	 * If the file is under to the root directory, returns a relative path.
	 * 
	 * @param file
	 *        The absolute file
	 * @return The relative file
	 * @see #getAbsoluteFile(File)
	 * @see #getRelativePath(String)
	 */
	public File getRelativeFile( File file )
	{
		return new File( getRelativePath( file.getPath() ) );
	}

	/**
	 * If the file is under to the root directory, returns a relative path.
	 * 
	 * @param file
	 *        The absolute file
	 * @return The relative path
	 * @see #getRelativeFile(File)
	 * @see #getRelativePath(File)
	 */
	public String getRelativePath( File file )
	{
		return getRelativePath( file.getAbsolutePath() );
	}

	/**
	 * If the path is under to the root directory, returns a relative path.
	 * 
	 * @param path
	 *        The absolute path
	 * @return The relative path
	 * @see #getRelativePath(File)
	 */
	public String getRelativePath( String path )
	{
		String root = this.root.getPath();
		if( path.startsWith( root ) )
			path = path.substring( root.length() + 1 );
		return path;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File root;
}

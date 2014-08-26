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

package com.threecrickets.sincerity.util;

import java.io.File;
import java.lang.reflect.Field;

/**
 * Native utilities.
 * 
 * @author Tal Liron
 */
public abstract class NativeUtil
{
	//
	// Static operations
	//

	/**
	 * Adds to the JVM's search path for native libraries.
	 * 
	 * @param nativePath
	 *        The path
	 */
	public static void addNativePath( File nativePath )
	{
		addNativePath( nativePath.getAbsolutePath() );
	}

	/**
	 * Adds to the JVM's search path for native libraries.
	 * 
	 * @param nativePath
	 *        The path
	 */
	public static void addNativePath( String nativePath )
	{
		try
		{
			// See:
			// http://fahdshariff.blogspot.com/2011/08/changing-java-library-path-at-runtime.html
			Field usrPathsField = ClassLoader.class.getDeclaredField( "usr_paths" );
			usrPathsField.setAccessible( true );
			String[] paths = (String[]) usrPathsField.get( null );
			boolean exists = false;
			for( String path : paths )
			{
				if( path.equals( nativePath ) )
				{
					exists = true;
					break;
				}
			}
			if( !exists )
			{
				String[] newPaths = new String[paths.length + 1];
				System.arraycopy( paths, 0, newPaths, 0, paths.length );
				newPaths[paths.length] = nativePath;
				usrPathsField.set( null, newPaths );
			}
		}
		catch( SecurityException e )
		{
		}
		catch( NoSuchFieldException e )
		{
		}
		catch( IllegalArgumentException e )
		{
		}
		catch( IllegalAccessException e )
		{
		}

		String javaLibraryPath = System.getProperty( "java.library.path" );
		String[] paths = javaLibraryPath.split( File.pathSeparator );
		boolean exists = false;
		for( String path : paths )
		{
			if( path.equals( nativePath ) )
			{
				exists = true;
				break;
			}
		}
		if( !exists )
		{
			javaLibraryPath += File.pathSeparator + nativePath;
			System.setProperty( "java.library.path", javaLibraryPath );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private NativeUtil()
	{
	}
}

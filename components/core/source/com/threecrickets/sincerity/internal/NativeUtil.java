package com.threecrickets.sincerity.internal;

import java.io.File;
import java.lang.reflect.Field;

public class NativeUtil
{
	public static void addNativePath( File nativePath )
	{
		addNativePath( nativePath.getAbsolutePath() );
	}

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
	}
}

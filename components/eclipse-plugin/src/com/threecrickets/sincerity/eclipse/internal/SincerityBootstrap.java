package com.threecrickets.sincerity.eclipse.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import com.threecrickets.sincerity.eclipse.SincerityPlugin;

public class SincerityBootstrap extends URLClassLoader
{
	//
	// Constants
	//

	public static final String MAIN_CLASS = "com.threecrickets.sincerity.Sincerity";

	//
	// Construction
	//

	public SincerityBootstrap( ClassLoader parent ) throws IOException
	{
		super( getUrls(), parent );
	}

	//
	// Operations
	//

	public void main( String... arguments ) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		Class<?> sincerityClass = loadClass( MAIN_CLASS );
		Method mainMethod = sincerityClass.getMethod( "main", String[].class );
		mainMethod.invoke( null, (Object) arguments );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static URL[] getUrls() throws IOException
	{
		ArrayList<URL> urls = new ArrayList<URL>();

		File homeDir = SincerityPlugin.getDefault().getSincerityHome();
		if( homeDir != null )
		{
			File jarsDir = new File( new File( homeDir, "libraries" ), "jars" );
			try
			{
				urls.add( new File( homeDir, "bootstrap.jar" ).toURI().toURL() );
			}
			catch( MalformedURLException x )
			{
			}
			listJars( jarsDir, urls );
		}

		return urls.toArray( new URL[urls.size()] );
	}

	private static void listJars( File file, ArrayList<URL> urls )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				listJars( child, urls );
		else if( file.getName().endsWith( ".jar" ) )
		{
			try
			{
				urls.add( file.toURI().toURL() );
			}
			catch( MalformedURLException x )
			{
			}
		}
	}
}

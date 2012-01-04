package com.threecrickets.sincerity;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class Bootstrap extends URLClassLoader
{
	public static void main( String[] arguments ) throws Exception
	{
		Bootstrap bootstrap = new Bootstrap();
		Thread.currentThread().setContextClassLoader( bootstrap );
		Class<?> theClass = Class.forName( "com.threecrickets.sincerity.Sincerity", true, bootstrap );
		Method mainMethod = theClass.getMethod( "main", String[].class );
		mainMethod.invoke( null, (Object) arguments );
	}

	public void addUrl( URL url )
	{
		for( URL existing : getURLs() )
			if( existing.equals( url ) )
				return;

		addURL( url );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Bootstrap()
	{
		super( getUrls(), Bootstrap.class.getClassLoader() );
	}

	private static URL[] getUrls()
	{
		String home = System.getProperty( "sincerity.home" );
		if( home == null )
		{
			System.err.println( "sincerity.home property is not set" );
			System.exit( 1 );
		}
		File homeDir = new File( home );
		if( !homeDir.isDirectory() )
		{
			System.err.println( "sincerity.home property does not point to a directory" );
			System.exit( 1 );
		}
		File librariesDir = new File( homeDir, "libraries" );
		if( !librariesDir.isDirectory() )
		{
			System.err.println( "sincerity.home property does not seem to point to a valid Sincerity installation" );
			System.exit( 1 );
		}

		ArrayList<URL> urls = new ArrayList<URL>();
		listJars( librariesDir, urls );
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

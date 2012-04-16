package com.threecrickets.sincerity.eclipse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

public class SincerityBootstrap extends URLClassLoader
{
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

	public void main( String... arguments )
	{
		try
		{
			Class<?> sincerityClass = loadClass( "com.threecrickets.sincerity.Sincerity" );
			Method mainMethod = sincerityClass.getMethod( "main", String[].class );
			mainMethod.invoke( null, (Object) arguments );
		}
		catch( Exception x )
		{
			x.printStackTrace();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String HOME_PROPERTY = "sincerity.home";

	private static final String HOME_VARIABLE = "SINCERITY_HOME";

	private static URL[] getUrls() throws IOException
	{
		String path = System.getProperty( HOME_PROPERTY );
		if( path == null )
			path = System.getenv( HOME_VARIABLE );

		File homeDir = new File( path ).getCanonicalFile();
		File jarsDir = new File( new File( homeDir, "libraries" ), "jars" );

		ArrayList<URL> urls = new ArrayList<URL>();
		try
		{
			urls.add( new File( homeDir, "bootstrap.jar" ).toURI().toURL() );
		}
		catch( MalformedURLException x )
		{
		}
		listJars( jarsDir, urls );
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

package com.threecrickets.bootstrap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A hypervisor for the JVM.
 * 
 * @author Tal Liron
 */
public class Bootstrap extends URLClassLoader
{
	//
	// Static attributes
	//

	public static Bootstrap getBootstrap()
	{
		return getBootstrap( null );
	}

	public static Bootstrap getBootstrap( File root )
	{
		return bootstraps.get( root );
	}

	public static void setBootstrap( File root, Bootstrap bootstrap )
	{
		bootstraps.put( root, bootstrap );
	}

	public static ConcurrentMap<Object, Object> getAttributes()
	{
		return attributes;
	}

	public static File getHome()
	{
		File homeDir = (File) getAttributes().get( "home" );
		if( homeDir == null )
		{
			String home = System.getProperty( HOME_PROPERTY );
			if( home == null )
				home = System.getenv( HOME_VARIABLE );
			if( home == null )
			{
				System.err.println( "Either the " + HOME_PROPERTY + " property or the " + HOME_VARIABLE + " environment variable must be set" );
				System.exit( 1 );
			}
			homeDir = new File( home );
			if( !homeDir.isDirectory() )
			{
				System.err.println( homeDir + " does not point to a directory" );
				System.exit( 1 );
			}
			try
			{
				homeDir = homeDir.getCanonicalFile();
			}
			catch( IOException x )
			{
				System.err.println( homeDir + " is not accessible" );
				System.exit( 1 );
			}
			getAttributes().put( "home", homeDir );
		}
		return homeDir;
	}

	//
	// Main
	//

	public static void main( String[] arguments ) throws Exception
	{
		Bootstrap bootstrap = new Bootstrap();
		setBootstrap( null, bootstrap );
		bootstrap( bootstrap, arguments );
	}

	public static void bootstrap( File root, String[] arguments ) throws Exception
	{
		bootstrap( getBootstrap( root ), arguments );
	}

	private static void bootstrap( ClassLoader classLoader, String[] arguments ) throws Exception
	{
		Thread.currentThread().setContextClassLoader( classLoader );
		Class<?> theClass = Class.forName( MAIN_CLASS, true, classLoader );
		Method mainMethod = theClass.getMethod( "main", String[].class );
		mainMethod.invoke( null, (Object) arguments );
	}

	//
	// Construction
	//

	public Bootstrap( Collection<URL> urls )
	{
		super( inheritUrls( urls ), Bootstrap.class.getClassLoader() );
	}

	//
	// Operations
	//

	public void addUrl( URL url )
	{
		for( URL existing : getURLs() )
			if( url.equals( existing ) )
				return;

		addURL( url );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String MAIN_CLASS;

	private static final String HOME_PROPERTY;

	private static final String HOME_VARIABLE;

	private static final ConcurrentMap<Object, Object> attributes = new ConcurrentHashMap<Object, Object>();

	private static final ConcurrentMap<File, Bootstrap> bootstraps = new ConcurrentHashMap<File, Bootstrap>();

	private Bootstrap()
	{
		super( getUrls(), Bootstrap.class.getClassLoader() );
	}

	private static URL[] inheritUrls( Collection<URL> urls )
	{
		ArrayList<URL> combined = new ArrayList<URL>( urls );
		Bootstrap bootstrap = getBootstrap();
		for( URL url : bootstrap.getURLs() )
			if( !combined.contains( url ) )
				combined.add( url );
		return combined.toArray( new URL[combined.size()] );
	}

	private static URL[] getUrls()
	{
		File homeDir = getHome();
		File jarsDir = new File( new File( homeDir, "libraries" ), "jars" );
		if( !jarsDir.isDirectory() )
		{
			System.err.println( homeDir + " does not seem to point to a valid installation" );
			System.exit( 1 );
		}

		ArrayList<URL> urls = new ArrayList<URL>();
		listJars( jarsDir, urls );

		// Add JVM classpath (is this necessary?)
		String system = System.getProperty( "java.class.path" );
		if( system != null )
		{
			for( String path : system.split( File.pathSeparator ) )
			{
				try
				{
					URL url = new File( path ).toURI().toURL();
					if( !urls.contains( url ) )
						urls.add( url );
				}
				catch( MalformedURLException x )
				{
				}
			}
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

	static
	{
		Properties properties = new Properties();
		InputStream stream = Bootstrap.class.getResourceAsStream( "bootstrap.properties" );
		try
		{
			properties.load( stream );
		}
		catch( IOException x )
		{
			System.err.println( "Could not read bootstrap.properties" );
			System.exit( 1 );
		}
		finally
		{
			try
			{
				stream.close();
			}
			catch( IOException x )
			{
			}
		}

		MAIN_CLASS = (String) properties.get( "main.class" );
		HOME_PROPERTY = (String) properties.get( "home.property" );
		HOME_VARIABLE = (String) properties.get( "home.variable" );
	}
}

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

	public static Bootstrap getMasterBootstrap()
	{
		return master;
	}

	public static Bootstrap getBootstrap( Object key )
	{
		return bootstraps.get( key );
	}

	public static void setBootstrap( Object key, Bootstrap bootstrap )
	{
		bootstraps.put( key, bootstrap );
	}

	public static ConcurrentMap<Object, Object> getAttributes()
	{
		return attributes;
	}

	public static File getHome()
	{
		File home = (File) getAttributes().get( "com.threecrickets.bootstrap.home" );
		if( home == null )
		{
			home = findHome();
			getAttributes().put( "com.threecrickets.bootstrap.home", home );
		}
		return home;
	}

	//
	// Main
	//

	public static void main( String[] arguments ) throws Exception
	{
		getMasterBootstrap().bootstrap( arguments );
	}

	public static void bootstrap( Object key, String[] arguments ) throws Exception
	{
		getBootstrap( key ).bootstrap( arguments );
	}

	//
	// Construction
	//

	/**
	 * Constructor for child bootstraps.
	 * 
	 * @param urls
	 */
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

	public void addFile( File file ) throws MalformedURLException
	{
		addUrl( file.toURI().toURL() );
	}

	public void bootstrap( String[] arguments ) throws Exception
	{
		Thread.currentThread().setContextClassLoader( this );
		Class<?> theClass = Class.forName( MAIN_CLASS, true, this );
		Method mainMethod = theClass.getMethod( "main", String[].class );
		mainMethod.invoke( null, (Object) arguments );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String MAIN_CLASS;

	private static final String HOME_PROPERTY;

	private static final String HOME_VARIABLE;

	private static final Bootstrap master;

	private static final ConcurrentMap<Object, Object> attributes;

	private static final ConcurrentMap<Object, Bootstrap> bootstraps;

	/**
	 * Constructor for the master bootstrap.
	 */
	private Bootstrap()
	{
		super( getUrls(), Bootstrap.class.getClassLoader() );
	}

	private static URL[] inheritUrls( Collection<URL> urls )
	{
		ArrayList<URL> combined = new ArrayList<URL>( urls );
		for( URL url : master.getURLs() )
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

		/*
		 * // Add JVM classpath String system = System.getProperty(
		 * "java.class.path" ); if( system != null ) { for( String path :
		 * system.split( File.pathSeparator ) ) { try { URL url = new File( path
		 * ).toURI().toURL(); if( !urls.contains( url ) ) urls.add( url ); }
		 * catch( MalformedURLException x ) { } } }
		 */

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

	private static File findHome()
	{
		String path = System.getProperty( HOME_PROPERTY );
		if( path == null )
			path = System.getenv( HOME_VARIABLE );
		if( path == null )
		{
			System.err.println( "Either the " + HOME_PROPERTY + " property or the " + HOME_VARIABLE + " environment variable must be set" );
			System.exit( 1 );
		}
		File home = new File( path );
		if( !home.isDirectory() )
		{
			System.err.println( home + " does not point to a directory" );
			System.exit( 1 );
		}
		try
		{
			home = home.getCanonicalFile();
		}
		catch( IOException x )
		{
			System.err.println( home + " is not accessible" );
			System.exit( 1 );
		}
		return home;
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

		attributes = new ConcurrentHashMap<Object, Object>();
		bootstraps = new ConcurrentHashMap<Object, Bootstrap>();
		master = new Bootstrap();
	}
}

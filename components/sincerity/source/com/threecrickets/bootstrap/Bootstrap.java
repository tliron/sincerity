/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

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
 * Bootstrap makes it easy to start a JVM application without having to set the
 * classpath first via a shell script.
 * <p>
 * Usually, you need to provide the classpath before invoking the JVM with your
 * application's main() entry point. With Bootstrap, you use Bootstrap's
 * {@link #main(String[])} instead. All you need to do is provide a base
 * directory for your jars, and the name of the class with your actual main()
 * entry point.
 * <p>
 * But Bootstrap offers another important benefit: it acts as a straightforward
 * hypervisor, letting you bootstrap several applications in the same JVM
 * instance, each with their own "class world." This lets you, in effect, run
 * several applications, either at the same time or in sequence, even if they
 * have overlapping and conflicting classes. The "master" bootstrap singleton
 * provides you with a space of shared classes, allowing the running
 * applications to share data structures.
 * <p>
 * One important use case for this is to let you "reboot" your application with
 * a new classpath, via a fresh bootstrap, without ever exiting the JVM.
 * 
 * @author Tal Liron
 */
public class Bootstrap extends URLClassLoader
{
	//
	// Static attributes
	//

	/**
	 * The master bootstrap.
	 * 
	 * @return The master bootstrap
	 */
	public static Bootstrap getMasterBootstrap()
	{
		return master;
	}

	/**
	 * A bootstrap according to a unique identifying key.
	 * 
	 * @param key
	 *        The key object (must qualify for use as a key in a hashmap)
	 * @return The bootstrap or null if not found
	 * @see #setBootstrap(Object, Bootstrap)
	 */
	public static Bootstrap getBootstrap( Object key )
	{
		return bootstraps.get( key );
	}

	/**
	 * @param key
	 *        The key object (must qualify for use as a key in a hashmap)
	 * @param bootstrap
	 *        The bootstrap
	 * @see #getBootstrap(Object)
	 */
	public static void setBootstrap( Object key, Bootstrap bootstrap )
	{
		bootstraps.put( key, bootstrap );
	}

	/**
	 * A general-purpose thread-safe location for global static attributes.
	 * 
	 * @return The attributes map
	 */
	public static ConcurrentMap<Object, Object> getAttributes()
	{
		return attributes;
	}

	/**
	 * The base directory for the master bootstrap. All shared Jars will be
	 * underneath this directory.
	 * 
	 * @return The base directory or null if not set
	 */
	public static File getHome()
	{
		File home = (File) getAttributes().get( "com.threecrickets.bootstrap.home" );
		if( home == null )
		{
			home = findHome();
			File existing = (File) getAttributes().putIfAbsent( "com.threecrickets.bootstrap.home", home );
			if( existing != null )
				home = existing;
		}
		return home;
	}

	//
	// Main
	//

	/**
	 * Delegates to your configured main() entry point through the master
	 * bootstrap.
	 * 
	 * @param arguments
	 *        Arguments to delegate to main
	 * @throws Exception
	 */
	public static void main( String[] arguments ) throws Exception
	{
		getMasterBootstrap().bootstrap( arguments );
	}

	/**
	 * Delegates to your configured main() entry point through any bootstrap.
	 * 
	 * @param key
	 *        The key object (must qualify for use as a key in a hashmap)
	 * @param arguments
	 *        Arguments to delegate to main
	 * @throws Exception
	 * @see #getBootstrap(Object)
	 */
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
		// super( inheritUrls( urls ), getMasterBootstrap() );
	}

	//
	// Operations
	//

	/**
	 * Adds a URL to the classpath. Checks to make sure there are no duplicates.
	 * 
	 * @param url
	 *        The URL
	 */
	public void addUrl( URL url )
	{
		for( URL existing : getURLs() )
			if( url.equals( existing ) )
				return;

		addURL( url );
	}

	/**
	 * Shortcut to add a file URL to the classpath.
	 * 
	 * @param file
	 *        The file
	 * @see #addUrl(URL)
	 */
	public void addFile( File file )
	{
		try
		{
			addUrl( file.toURI().toURL() );
		}
		catch( MalformedURLException x )
		{
		}
	}

	/**
	 * Recursively adds a directory and all Jar files underneath it to the
	 * classpath.
	 * <p>
	 * Note that recursion only happens if you add a directory, otherwise it
	 * behaves like {@link #addFile(File)}).
	 * 
	 * @param file
	 *        The file (special behavior if the file is a directory)
	 */
	public void addJars( File file )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				addJars( child );
		else if( file.getName().endsWith( ".jar" ) )
			addFile( file );
	}

	/**
	 * Delegates to your configured main() entry point through the bootstrap.
	 * 
	 * @param arguments
	 *        Arguments to delegate to main
	 * @throws Exception
	 */
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
		InputStream stream = Bootstrap.class.getResourceAsStream( "bootstrap.conf" );
		try
		{
			properties.load( stream );
		}
		catch( IOException x )
		{
			System.err.println( "Could not read bootstrap.conf" );
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

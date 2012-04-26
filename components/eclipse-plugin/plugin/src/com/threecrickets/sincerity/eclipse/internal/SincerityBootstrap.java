package com.threecrickets.sincerity.eclipse.internal;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IVMInstall;

import com.threecrickets.sincerity.eclipse.SincerityPlugin;

public class SincerityBootstrap extends URLClassLoader
{
	//
	// Constants
	//

	public static final String MAIN_CLASS = "com.threecrickets.sincerity.Sincerity";

	//
	// Static operations
	//

	public static boolean run( String... arguments )
	{
		try
		{
			if( SincerityPlugin.getDefault().getUseAlternateJre() )
			{
				IVMInstall install = SincerityPlugin.getDefault().getAlternateJre();
				if( install == null )
					return false;
				SincerityPlugin.getSimpleLog().log( IStatus.INFO, "Running Sincerity on external JRE: " + install.getName() );
				EclipseUtil.run( install, getJarPaths(), MAIN_CLASS, arguments );
				return true;
			}
			else
			{
				SincerityBootstrap sincerityBoostrap = new SincerityBootstrap( SincerityBootstrap.class.getClassLoader() );
				sincerityBoostrap.main( arguments );
				return true;
			}
		}
		catch( Exception x )
		{
			SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
		}

		return false;
	}

	//
	// Construction
	//

	public SincerityBootstrap( ClassLoader parent ) throws IOException
	{
		super( getJarUrls(), parent );
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

	private static URL[] getJarUrls() throws IOException
	{
		File[] files = getJarFiles();
		int length = files.length;
		URL[] urls = new URL[length];
		for( int i = 0; i < length; i++ )
		{
			try
			{
				urls[i] = files[i].toURI().toURL();
			}
			catch( MalformedURLException x )
			{
			}
		}
		return urls;
	}

	private static String[] getJarPaths() throws IOException
	{
		File[] files = getJarFiles();
		int length = files.length;
		String[] paths = new String[length];
		for( int i = 0; i < length; i++ )
			paths[i] = files[i].getPath();
		return paths;
	}

	private static File[] getJarFiles() throws IOException
	{
		ArrayList<File> jars = new ArrayList<File>();

		File homeDir = SincerityPlugin.getDefault().getSincerityRoot();
		if( homeDir != null )
		{
			File jarsDir = new File( new File( homeDir, "libraries" ), "jars" );
			jars.add( new File( homeDir, "bootstrap.jar" ) );
			listJars( jarsDir, jars );
		}

		return jars.toArray( new File[jars.size()] );
	}

	private static void listJars( File file, ArrayList<File> jars )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				listJars( child, jars );
		else if( file.getName().endsWith( ".jar" ) )
			jars.add( file );
	}
}

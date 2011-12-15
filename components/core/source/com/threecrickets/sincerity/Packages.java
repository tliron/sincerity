package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnpackingException;

public class Packages extends ArrayList<Package>
{
	//
	// Constants
	//

	public static final String MANIFEST = "META-INF/MANIFEST.MF";

	public static final String PACKAGE_FOLDERS = "Package-Folders";

	public static final String PACKAGE_FILES = "Package-Files";

	public static final String PACKAGE_RESOURCES = "Package-Resources";

	//
	// Construction
	//

	public Packages( File root, ClassLoader classLoader, Container container ) throws SincerityException
	{
		try
		{
			Enumeration<URL> resources = classLoader.getResources( MANIFEST );
			while( resources.hasMoreElements() )
			{
				URL resource = resources.nextElement();
				InputStream stream = resource.openStream();
				try
				{
					Attributes manifest = new Manifest( stream ).getMainAttributes();
					Package pack = null;

					Object packageFoldersAttribute = manifest.getValue( PACKAGE_FOLDERS );
					if( packageFoldersAttribute != null )
					{
						if( !"jar".equals( resource.getProtocol() ) )
							throw new UnpackingException( "Package folders " + packageFoldersAttribute + " is not in a jar file: " + resource );

						JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
						ArrayList<JarEntry> entries = getJarEntries( jarConnection );

						if( pack == null )
						{
							pack = new Package( container.getRelativeFile( new File( jarConnection.getJarFileURL().toURI() ) ) );
							add( pack );
						}

						String packageFolders = packageFoldersAttribute.toString();
						for( String packageFolder : packageFolders.split( "," ) )
						{
							String prefix = packageFolder + "/";
							int prefixLength = prefix.length();

							URL urlContext = new URL( "jar:" + jarConnection.getJarFileURL() + "!/" + packageFolder );
							for( JarEntry entry : entries )
							{
								String name = entry.getName();
								if( name.startsWith( prefix ) && name.length() > prefixLength )
								{
									URL url = new URL( urlContext, name );
									pack.add( new Artifact( new File( root, name.substring( prefixLength ) ), url, container ) );
								}
							}
						}
					}

					Object packageFilesAttribute = manifest.getValue( PACKAGE_FILES );
					if( packageFilesAttribute != null )
					{
						if( !"jar".equals( resource.getProtocol() ) )
							throw new UnpackingException( "Package files " + packageFilesAttribute + " is not in a jar file: " + resource );

						JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
						ArrayList<JarEntry> entries = getJarEntries( jarConnection );

						if( pack == null )
						{
							pack = new Package( container.getRelativeFile( new File( jarConnection.getJarFileURL().toURI() ) ) );
							add( pack );
						}

						String packageFiles = packageFilesAttribute.toString();
						for( String packageFile : packageFiles.split( "," ) )
						{
							boolean found = false;
							for( JarEntry entry : entries )
							{
								if( packageFile.equals( entry.getName() ) )
								{
									URL url = new URL( "jar:" + jarConnection.getJarFileURL() + "!/" + packageFile );
									pack.add( new Artifact( new File( root, packageFile ), url, container ) );
									found = true;
									break;
								}
							}
							if( !found )
								throw new UnpackingException( "Package file " + packageFile + " not found in " + pack.getFile() );
						}
					}

					Object packageResourcesAttribute = manifest.getValue( PACKAGE_RESOURCES );
					if( packageResourcesAttribute != null )
					{
						if( pack == null )
						{
							if( !"jar".equals( resource.getProtocol() ) )
								throw new UnpackingException( "Package resources " + packageFoldersAttribute + " is not in a jar file: " + resource );

							JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
							pack = new Package( container.getRelativeFile( new File( jarConnection.getJarFileURL().toURI() ) ) );
							add( pack );
						}

						for( String name : packageResourcesAttribute.toString().split( "," ) )
						{
							URL url = classLoader.getResource( name );
							if( url == null )
								throw new UnpackingException( "Could not find packaged resource " + name + " from " + pack.getFile() );

							pack.add( new Artifact( new File( root, name ), url, container ) );
						}
					}
				}
				finally
				{
					stream.close();
				}
			}
		}
		catch( MalformedURLException x )
		{
			throw new UnpackingException( "Parsing error while looking for packages", x );
		}
		catch( URISyntaxException x )
		{
			throw new UnpackingException( "Parsing error while looking for packages", x );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "I/O error while looking for packages", x );
		}
	}

	//
	// Attributes
	//

	public Package getByPackage( File file )
	{
		for( Package pack : this )
			if( file.equals( pack.getFile() ) )
				return pack;
		return null;
	}

	public Package getByPacked( File file )
	{
		for( Package pack : this )
			for( Artifact artifact : pack )
				if( file.equals( artifact.getFile() ) )
					return pack;
		return null;
	}

	//
	// Operations
	//

	public void unpack( String filter, boolean overwrite ) throws UnpackingException
	{
		for( Package pack : this )
			pack.unpack( filter, overwrite );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private static ArrayList<JarEntry> getJarEntries( JarURLConnection jarConnection ) throws UnpackingException
	{
		try
		{
			JarFile jarFile = jarConnection.getJarFile();
			ArrayList<JarEntry> entries = new ArrayList<JarEntry>();
			for( Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); )
			{
				JarEntry entry = e.nextElement();
				if( !entry.isDirectory() )
					entries.add( entry );
			}
			return entries;
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not unpack jar file: " + jarConnection.getJarFileURL(), x );
		}
	}
}

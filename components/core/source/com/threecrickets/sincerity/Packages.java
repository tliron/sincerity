package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
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
							throw new UnpackingException( "Package folders are not in a jar: " + packageFoldersAttribute + ", manifest: " + manifest );

						JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
						ArrayList<JarEntry> entries = getJarEntries( jarConnection );

						if( pack == null )
						{
							pack = new Package();
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
							throw new UnpackingException( "Package files are not in a jar: " + packageFilesAttribute + ", manifest: " + manifest );

						JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
						ArrayList<JarEntry> entries = getJarEntries( jarConnection );

						if( pack == null )
						{
							pack = new Package();
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
								throw new UnpackingException( "Package file " + packageFile + " not found in " + jarConnection.getJarFileURL() );
						}
					}

					Object packageResourcesAttribute = manifest.getValue( PACKAGE_RESOURCES );
					if( packageResourcesAttribute != null )
					{
						if( pack == null )
						{
							pack = new Package();
							add( pack );
						}

						for( String name : packageResourcesAttribute.toString().split( "," ) )
						{
							URL url = classLoader.getResource( name );
							if( url == null )
								throw new UnpackingException( "Could not find packaged resource " + name + " specified in manifest " + manifest );

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
		catch( IOException x )
		{
			throw new UnpackingException( "I/O error while looking for packages", x );
		}
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

package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.threecrickets.sincerity.exception.InstallationException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnpackingException;
import com.threecrickets.sincerity.internal.ClassUtil;

public class Package extends AbstractList<Artifact>
{
	//
	// Constants
	//

	public static final String PACKAGE_FOLDERS = "Package-Folders";

	public static final String PACKAGE_FILES = "Package-Files";

	public static final String PACKAGE_RESOURCES = "Package-Resources";

	public static final String PACKAGE_INSTALLER = "Package-Installer";

	public static final String PACKAGE_UNINSTALLER = "Package-Uninstaller";

	//
	// Construction
	//

	public static Package createPackage( URL manifestUrl, Container container ) throws SincerityException
	{
		String installer = null;
		String uninstaller = null;
		ArrayList<Artifact> artifacts = new ArrayList<Artifact>();

		File root = container.getRoot();
		Jar jar = null;

		try
		{
			InputStream stream = manifestUrl.openStream();
			try
			{
				Attributes manifest = new Manifest( stream ).getMainAttributes();

				Object packageInstallerAttribute = manifest.getValue( PACKAGE_INSTALLER );
				if( packageInstallerAttribute != null )
					installer = packageInstallerAttribute.toString();

				Object packageUninstallerAttribute = manifest.getValue( PACKAGE_UNINSTALLER );
				if( packageUninstallerAttribute != null )
					uninstaller = packageUninstallerAttribute.toString();

				Object packageFoldersAttribute = manifest.getValue( PACKAGE_FOLDERS );
				if( packageFoldersAttribute != null )
				{
					if( jar == null )
						jar = new Jar( manifestUrl, container, "Package folders " + packageFoldersAttribute );

					for( String packageFolder : packageFoldersAttribute.toString().split( "," ) )
					{
						String prefix = packageFolder + "/";
						int prefixLength = prefix.length();

						URL urlContext = new URL( "jar:" + jar.url + "!/" + packageFolder );
						for( JarEntry entry : jar.entries )
						{
							String name = entry.getName();
							if( name.startsWith( prefix ) && name.length() > prefixLength )
							{
								URL url = new URL( urlContext, name );
								artifacts.add( new Artifact( new File( root, name.substring( prefixLength ) ), url, container ) );
							}
						}
					}
				}

				Object packageFilesAttribute = manifest.getValue( PACKAGE_FILES );
				if( packageFilesAttribute != null )
				{
					if( jar == null )
						jar = new Jar( manifestUrl, container, "Package files " + packageFilesAttribute );

					for( String packageFile : packageFilesAttribute.toString().split( "," ) )
					{
						boolean found = false;
						for( JarEntry entry : jar.entries )
						{
							if( packageFile.equals( entry.getName() ) )
							{
								URL url = new URL( "jar:" + jar.url + "!/" + packageFile );
								artifacts.add( new Artifact( new File( root, packageFile ), url, container ) );
								found = true;
								break;
							}
						}
						if( !found )
							throw new UnpackingException( "Package file " + packageFile + " not found in " + jar.file );
					}
				}

				Object packageResourcesAttribute = manifest.getValue( PACKAGE_RESOURCES );
				if( packageResourcesAttribute != null )
				{
					if( jar == null )
						jar = new Jar( manifestUrl, container, "Package resources " + packageResourcesAttribute );

					ClassLoader classLoader = container.getDependencies().getClassLoader();
					for( String name : packageResourcesAttribute.toString().split( "," ) )
					{
						URL url = classLoader.getResource( name );
						if( url == null )
							throw new UnpackingException( "Could not find packaged resource " + name + " from " + jar.file );

						artifacts.add( new Artifact( new File( root, name ), url, container ) );
					}
				}
			}
			finally
			{
				stream.close();
			}
		}
		catch( MalformedURLException x )
		{
			throw new UnpackingException( "Parsing error in package: " + manifestUrl, x );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "I/O error in package: " + manifestUrl, x );
		}

		if( installer == null && uninstaller == null && artifacts.isEmpty() )
			return null;

		return new Package( installer, uninstaller, jar != null ? jar.file : null, artifacts );
	}

	//
	// Attributes
	//

	public String getInstaller()
	{
		return installer;
	}

	public String getUninstaller()
	{
		return uninstaller;
	}

	public File getFile()
	{
		return file;
	}

	//
	// Operations
	//

	public void install() throws SincerityException
	{
		if( installer != null )
		{
			String[] arguments = installer.split( " " );
			try
			{
				ClassUtil.main( null, arguments );
			}
			catch( SincerityException x )
			{
				throw new InstallationException( "Could not install package: " + file, x );
			}
		}
	}

	public void uninstall() throws SincerityException
	{
		if( uninstaller != null )
		{
			String[] arguments = uninstaller.split( " " );
			ClassUtil.main( null, arguments );
		}
	}

	public void unpack( String filter, boolean overwrite ) throws UnpackingException
	{
		for( Artifact artifact : this )
			artifact.unpack( filter, overwrite );
	}

	//
	// AbstractList
	//

	@Override
	public int size()
	{
		return artifacts.size();
	}

	@Override
	public Artifact get( int index )
	{
		if( !sorted )
		{
			Collections.sort( artifacts );
			sorted = true;
		}
		return artifacts.get( index );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String installer;

	private final String uninstaller;

	private final File file;

	private final List<Artifact> artifacts;

	public boolean sorted;

	private Package( String installer, String uninstaller, File file, List<Artifact> artifacts )
	{
		super();
		this.installer = installer;
		this.uninstaller = uninstaller;
		this.file = file;
		this.artifacts = artifacts;
	}

	private static class Jar
	{
		public Jar( URL manifestUrl, Container container, String errorMessage ) throws UnpackingException
		{
			if( !"jar".equals( manifestUrl.getProtocol() ) )
				throw new UnpackingException( errorMessage + " is not in a jar file: " + manifestUrl );

			JarURLConnection connection;
			try
			{
				connection = (JarURLConnection) manifestUrl.openConnection();
			}
			catch( IOException x )
			{
				throw new UnpackingException( "Could not read jar file: " + manifestUrl, x );
			}

			url = connection.getJarFileURL();
			try
			{
				file = container.getRelativeFile( new File( url.toURI() ) );
			}
			catch( URISyntaxException x )
			{
				throw new UnpackingException( "Parsing error in package: " + manifestUrl, x );
			}

			try
			{
				JarFile jarFile = connection.getJarFile();
				entries = new ArrayList<JarEntry>( jarFile.size() );
				for( Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); )
				{
					JarEntry entry = e.nextElement();
					if( !entry.isDirectory() )
						entries.add( entry );
				}
			}
			catch( IOException x )
			{
				throw new UnpackingException( "Could not unpack jar file: " + file, x );
			}
		}

		public final URL url;

		public final File file;

		public final ArrayList<JarEntry> entries;
	}
}
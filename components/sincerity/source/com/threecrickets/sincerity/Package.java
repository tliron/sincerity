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

/**
 * Packages are collections of artifacts (see {@link Artifact}). They are
 * defined using special tags in standard JVM resource manifests. Additionally,
 * packages support special install/uninstall hooks for calling arbitrary entry
 * points, allowing for custom behavior. Indeed, a package can include no
 * artifacts, and only implement these hooks.
 * <p>
 * Packages allow you to work around various limitations in repositories such as
 * iBiblio/Maven, in which the smallest deployable unit is a Jar. The package
 * specification allows you to include as many files as you need in a single
 * Jar, greatly simplifying your deployment scheme.
 * <p>
 * Note that two different ways are supported for specifying artifacts: they can
 * specified as files, thus referring to actual zipped entries with the Jar file
 * in which the manifest resides, or that can be specified as general resources,
 * in which case they will be general resource URLs to be loaded by the
 * classloader, and thus they can reside anywhere in the classpath.
 * <p>
 * Also note what "volatile" means in this context: a "volatile" artifact is one
 * that should be installed once and only once. This means that subsequent
 * attempts to install the package, beyond the first, should ignore these
 * artifacts. This is useful for marking configuration files, example files, and
 * other files that the user should be allow to delete without worrying that
 * they would reappear on every change to the dependency structure.
 * <p>
 * Supported manifest tags:
 * <ul>
 * <li><b>Package-Files</b>: a comma separated list of file paths within this
 * Jar.</li>
 * <li><b>Package-Folders</b>: a comma separated list of folder paths within
 * this Jar. Specifies all artifacts under these folders, recursively.</li>
 * <li><b>Package-Resources</b>: a comma separated list of resource paths to be
 * retrieved via the classloader.</li>
 * <li><b>Package-Volatile-Files</b>: all these artifacts will be marked as
 * volatile.</li>
 * <li><b>Package-Volatile-Folders</b>: all artifacts under these paths will be
 * marked as volatile.</li>
 * <li><b>Package-Installer</b>: specifies a class name which has a main() entry
 * point. Simple string arguments can be optionally appended, separated by
 * spaces. The installer will be called when the package is to be installed,
 * <i>after</i> all artifacts have been unpacked. Any thrown exception would
 * cause installation to fail.</li>
 * <li><b>Package-Uninstaller</b>: specifies a class name which has a main()
 * entry point. Simple string arguments can be optionally appended, separated by
 * spaces. The uninstaller will be called when the package is to be uninstalled.
 * </li>
 * </ul>
 * 
 * @author Tal Liron
 * @see Packages
 * @see Manifest
 */
public class Package extends AbstractList<Artifact>
{
	//
	// Constants
	//

	public static final String PACKAGE_FOLDERS = "Package-Folders";

	public static final String PACKAGE_VOLATILE_FOLDERS = "Package-Volatile-Folders";

	public static final String PACKAGE_FILES = "Package-Files";

	public static final String PACKAGE_VOLATILE_FILES = "Package-Volatile-Files";

	public static final String PACKAGE_RESOURCES = "Package-Resources";

	public static final String PACKAGE_INSTALLER = "Package-Installer";

	public static final String PACKAGE_UNINSTALLER = "Package-Uninstaller";

	//
	// Construction
	//

	public static Package parsePackage( URL manifestUrl, Container container ) throws SincerityException
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

				Volatiles volatiles = null;

				Object packageFoldersAttribute = manifest.getValue( PACKAGE_FOLDERS );
				if( packageFoldersAttribute != null )
				{
					if( jar == null )
						jar = new Jar( manifestUrl, container, "Package folders " + packageFoldersAttribute );
					if( volatiles == null )
						volatiles = new Volatiles( manifest );

					for( String packageFolder : packageFoldersAttribute.toString().split( "," ) )
					{
						String prefix = packageFolder;
						if( !prefix.endsWith( "/" ) )
							prefix += "/";
						int prefixLength = prefix.length();

						URL urlContext = new URL( "jar:" + jar.url + "!/" + packageFolder );
						for( JarEntry entry : jar.entries )
						{
							String name = entry.getName();
							if( name.startsWith( prefix ) && name.length() > prefixLength )
							{
								URL url = new URL( urlContext, name );
								artifacts.add( new Artifact( new File( root, name.substring( prefixLength ) ), url, volatiles.contains( name ), container ) );
							}
						}
					}
				}

				Object packageFilesAttribute = manifest.getValue( PACKAGE_FILES );
				if( packageFilesAttribute != null )
				{
					if( jar == null )
						jar = new Jar( manifestUrl, container, "Package files " + packageFilesAttribute );
					if( volatiles == null )
						volatiles = new Volatiles( manifest );

					for( String packageFile : packageFilesAttribute.toString().split( "," ) )
					{
						boolean found = false;
						for( JarEntry entry : jar.entries )
						{
							if( packageFile.equals( entry.getName() ) )
							{
								URL url = new URL( "jar:" + jar.url + "!/" + packageFile );
								artifacts.add( new Artifact( new File( root, packageFile ), url, volatiles.contains( packageFile ), container ) );
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
					if( volatiles == null )
						volatiles = new Volatiles( manifest );

					ClassLoader classLoader = container.getBootstrap();
					for( String name : packageResourcesAttribute.toString().split( "," ) )
					{
						URL url = classLoader.getResource( name );
						if( url == null )
							throw new UnpackingException( "Could not find packaged resource " + name + " from " + jar.file );

						artifacts.add( new Artifact( new File( root, name ), url, volatiles.contains( name ), container ) );
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

	public void install( String filter, boolean overwrite ) throws UnpackingException
	{
		for( Artifact artifact : this )
			artifact.install( filter, overwrite );
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

	private static class Volatiles
	{
		public Volatiles( Attributes manifest )
		{
			Object packageVolatileFoldersAttribute = manifest.getValue( PACKAGE_VOLATILE_FOLDERS );
			folders = packageVolatileFoldersAttribute != null ? packageVolatileFoldersAttribute.toString().split( "," ) : null;

			Object packageVolatileFilesAttribute = manifest.getValue( PACKAGE_VOLATILE_FILES );
			files = packageVolatileFilesAttribute != null ? packageVolatileFilesAttribute.toString().split( "," ) : null;

			if( folders != null )
				for( int i = folders.length - 1; i >= 0; i-- )
					if( !folders[i].endsWith( "/" ) )
						folders[i] += "/";
		}

		public boolean contains( String name )
		{
			if( folders != null )
				for( String folder : folders )
					if( name.startsWith( folder ) )
						return true;
			if( files != null )
				for( String file : files )
					if( name.equals( file ) )
						return true;
			return false;
		}

		private final String[] folders;

		private final String[] files;
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
/**
 * Copyright 2011-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.packaging;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.Manifest;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnpackingException;

/**
 * Manages the packages defined in a classpath.
 * <p>
 * This class works by scanning the entire classpath for packages defined in the
 * JVM resources manifests, and parsing them. See {@link Package} for more
 * information.
 * 
 * @author Tal Liron
 * @see Manifest
 */
public class Packages extends ArrayList<Package>
{
	//
	// Constants
	//

	public static final String MANIFEST = "META-INF/MANIFEST.MF";

	//
	// Construction
	//

	/**
	 * Initializes all packages in the container's classpath.
	 * 
	 * @param packagingContext
	 *        The packaging context
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Packages( PackagingContext packagingContext ) throws SincerityException
	{
		try
		{
			Enumeration<URL> manifestUrls = packagingContext.getClassLoader().getResources( MANIFEST );
			while( manifestUrls.hasMoreElements() )
			{
				URL manifestUrl = manifestUrls.nextElement();
				Package pack = Package.parsePackage( manifestUrl, packagingContext );
				if( pack != null )
					add( pack );
			}
		}
		catch( IOException x )
		{
			throw new UnpackingException( "I/O error while looking for packages", x );
		}
	}

	//
	// Attributes
	//

	/**
	 * Finds a package based on its package file (a Jar).
	 * 
	 * @param file
	 *        The package file
	 * @return The package or null
	 */
	public Package getPackage( File file )
	{
		for( Package pack : this )
			if( file.equals( pack.getFile() ) )
				return pack;
		return null;
	}

	/**
	 * Finds the package that contains the artifact.
	 * 
	 * @param file
	 *        The artifact file
	 * @return The package or null
	 */
	public Package getPackageForArtifact( File file )
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

	/**
	 * Unpacks all artifacts and runs package installers.
	 * 
	 * @param managedArtifacts
	 *        The managed artifacts
	 * @param filter
	 *        Filter artifacts (currently unused)
	 * @param overwrite
	 *        True to force overwriting of modified artifacts
	 * @param verify
	 *        Whether to verify the unpacking
	 * @throws SincerityException
	 *         In case of an error
	 * @see Package#unpack(String, ArtifactManager, boolean, boolean)
	 */
	public void install( ArtifactManager managedArtifacts, String filter, boolean overwrite, boolean verify ) throws SincerityException
	{
		for( Package pack : this )
		{
			pack.unpack( filter, managedArtifacts, overwrite, verify );
			pack.install();
		}
	}

	/**
	 * Runs all package uninstallers.
	 * <p>
	 * Does <i>not</i> prune previously installed artifacts.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void uninstall() throws SincerityException
	{
		for( Package pack : this )
			pack.uninstall();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

/**
 * Copyright 2011-2013 Three Crickets LLC.
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
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.Manifest;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnpackingException;

/**
 * Manages the packages defined in the classpath of a {@link Dependencies}
 * instance.
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

	public Packages( Container container ) throws SincerityException
	{
		this.container = container;
		try
		{
			Enumeration<URL> manifestUrls = container.getBootstrap().getResources( MANIFEST );
			while( manifestUrls.hasMoreElements() )
			{
				URL manifestUrl = manifestUrls.nextElement();
				Package pack = Package.parsePackage( manifestUrl, container );
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

	public void unpack( String filter, boolean overwrite ) throws SincerityException
	{
		for( Package pack : this )
		{
			pack.unpack( filter, container.getDependencies().getManagedArtifacts(), overwrite );
			pack.install();
		}
	}

	public void uninstall() throws SincerityException
	{
		for( Package pack : this )
			pack.uninstall();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final Container container;
}

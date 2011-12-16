package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnpackingException;

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
		try
		{
			Enumeration<URL> manifestUrls = container.getDependencies().getClassLoader().getResources( MANIFEST );
			while( manifestUrls.hasMoreElements() )
			{
				URL manifestUrl = manifestUrls.nextElement();
				Package pack = Package.createPackage( manifestUrl, container );
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

	public void unpack( String filter, boolean overwrite ) throws UnpackingException
	{
		for( Package pack : this )
			pack.unpack( filter, overwrite );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

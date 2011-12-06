package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Packages extends HashMap<String, Package>
{
	//
	// Construction
	//

	public Packages( File root, ClassLoader classLoader ) throws IOException
	{
		Enumeration<URL> resources = classLoader.getResources( "META-INF/MANIFEST.MF" );
		while( resources.hasMoreElements() )
		{
			URL resource = resources.nextElement();
			InputStream stream = resource.openStream();
			try
			{
				Attributes manifest = new Manifest( stream ).getMainAttributes();
				Object packageNameAttribute = manifest.getValue( "Package-Name" );
				if( packageNameAttribute != null )
				{
					String packageName = packageNameAttribute.toString();
					Package pack = new Package();
					put( packageName, pack );

					Object packageContentsAttribute = manifest.getValue( "Package-Contents" );
					if( packageContentsAttribute != null )
					{
						for( String name : packageContentsAttribute.toString().split( "," ) )
						{
							URL url = classLoader.getResource( packageName + "/" + name );
							if( url == null )
								System.err.println( "Could not find package content: " + packageName + "/" + name );
							else
								pack.add( new Artifact( new File( root, name ), url ) );
						}
					}
					else if( "jar".equals( resource.getProtocol() ) )
					{
						JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
						JarFile jarFile = jarConnection.getJarFile();
						URL urlContext = new URL( "jar:" + jarConnection.getJarFileURL() + "!/" + packageName );
						String prefix = packageName + "/";
						int prefixLength = prefix.length();
						for( Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); )
						{
							JarEntry entry = entries.nextElement();
							if( !entry.isDirectory() )
							{
								String name = entry.getName();
								if( name.startsWith( prefix ) && name.length() > prefixLength )
								{
									URL url = new URL( urlContext, name );
									pack.add( new Artifact( new File( root, name.substring( prefixLength ) ), url ) );
								}
							}
						}
					}
				}
			}
			finally
			{
				stream.close();
			}
		}
	}

	//
	// Operations
	//

	public void unpack( boolean overwrite ) throws IOException
	{
		for( Package pack : values() )
			pack.unpack( overwrite );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

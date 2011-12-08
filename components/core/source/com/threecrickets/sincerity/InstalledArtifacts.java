package com.threecrickets.sincerity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Properties;

public class InstalledArtifacts
{
	//
	// Constants
	//

	public final static int MODE_UPDATE_ONLY = 0;

	public final static int MODE_PRUNE = 1;

	public final static int MODE_CLEAN = 2;

	//
	// Construction
	//

	public InstalledArtifacts( File file, Container container )
	{
		this.file = file;
		this.container = container;
	}

	//
	// Attributes
	//

	public boolean isPresent( Artifact artifact ) throws IOException
	{
		validate();

		Object value = properties.get( artifact.getFile().getPath() );
		if( value != null )
		{
			String[] values = value.toString().split( ",", 2 );
			return "true".equals( values[0] );
		}

		return false;
	}

	//
	// Operations
	//

	public void update( Iterable<Artifact> artifacts, int mode ) throws ParseException, IOException
	{
		validate();

		// Mark all artifacts for removal
		for( Object key : properties.keySet() )
		{
			String path = key.toString();
			String[] values = properties.get( path ).toString().split( ",", 2 );
			String url = values.length > 1 ? values[1] : null;
			String value = url == null ? "false" : "false," + url;
			properties.put( path, value );
		}

		if( mode != MODE_CLEAN )
		{
			// Mark all existing artifacts as present
			for( Artifact artifact : artifacts )
			{
				URL url = artifact.getUrl();
				String value = url == null ? "true" : "true," + url;
				properties.put( artifact.getFile().getPath(), value );
			}
		}

		if( mode != MODE_UPDATE_ONLY )
		{
			for( Object key : new HashSet<Object>( properties.keySet() ) )
			{
				String path = key.toString();
				String[] values = properties.get( path ).toString().split( ",", 2 );
				boolean keep = "true".equals( values[0] );
				String url = values.length > 1 ? values[1] : null;

				if( !keep )
				{
					properties.remove( path );

					File file = new File( path );
					if( !file.exists() )
						continue;

					if( url != null )
					{
						// Keep changed artifacts
						Artifact artifact = new Artifact( new File( path ), new URL( url ), container );
						if( artifact.isDifferent() )
						{
							System.out.println( "Keeping changed file: " + path );
							continue;
						}
					}

					System.out.println( "Deleting: " + container.getRelativePath( path ) );
					file.delete();
				}
			}
		}

		FileOutputStream stream = new FileOutputStream( file );
		try
		{
			properties.store( stream, "Managed by Sincerity" );
		}
		finally
		{
			stream.close();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private final Container container;

	private Properties properties;

	private void validate() throws IOException
	{
		if( properties == null )
		{
			properties = new Properties();
			try
			{
				FileInputStream stream = new FileInputStream( file );
				try
				{
					properties.load( stream );
				}
				finally
				{
					stream.close();
				}
			}
			catch( FileNotFoundException x )
			{
			}
		}
	}
}

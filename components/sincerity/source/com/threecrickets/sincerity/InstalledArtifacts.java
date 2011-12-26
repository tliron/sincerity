package com.threecrickets.sincerity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;

import com.threecrickets.sincerity.exception.SincerityException;

public class InstalledArtifacts
{
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

	public boolean isKept( Artifact artifact ) throws SincerityException
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

	public void update( Iterable<Artifact> artifacts ) throws SincerityException
	{
		update( artifacts, MODE_UPDATE );
	}

	public void add( Iterable<Artifact> artifacts ) throws SincerityException
	{
		update( artifacts, MODE_ADD );
	}

	public void clean() throws SincerityException
	{
		update( null, MODE_CLEAN );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final static int MODE_UPDATE = 0;

	private final static int MODE_ADD = 1;

	private final static int MODE_CLEAN = 2;

	private final File file;

	private final Container container;

	private Properties properties;

	private void validate() throws SincerityException
	{
		if( properties == null )
		{
			properties = new Properties();
			try
			{
				FileInputStream stream = new FileInputStream( file );
				try
				{
					try
					{
						properties.load( stream );
					}
					finally
					{
						stream.close();
					}
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not read artifacts configuration", x );
				}
			}
			catch( FileNotFoundException x )
			{
			}
		}
	}

	private void update( Iterable<Artifact> artifacts, int mode ) throws SincerityException
	{
		validate();

		// The format is "container_path = kept,source_url"

		// Mark all current artifacts as not kept
		for( Object key : properties.keySet() )
		{
			String path = key.toString();
			String[] values = properties.get( path ).toString().split( ",", 2 );
			String url = values.length > 1 ? values[1] : null;
			String value = url == null ? "false" : "false," + url;
			properties.put( path, value );
		}

		if( ( mode == MODE_ADD ) || ( mode == MODE_UPDATE ) )
		{
			// Mark the new set of artifacts as kept
			for( Artifact artifact : artifacts )
			{
				URL url = artifact.getUrl();
				String value = url == null ? "true" : "true," + url;
				properties.put( artifact.getPath(), value );
			}
		}

		if( ( mode == MODE_CLEAN ) || ( mode == MODE_UPDATE ) )
		{
			for( Object key : new HashSet<Object>( properties.keySet() ) )
			{
				String path = key.toString();
				String[] values = properties.get( path ).toString().split( ",", 2 );
				boolean kept = "true".equals( values[0] );
				String url = values.length > 1 ? values[1] : null;

				if( !kept )
				{
					properties.remove( path );

					File file = new File( path );
					if( !file.isAbsolute() )
						file = container.getFile( path );

					if( !file.exists() )
						continue;

					if( url != null )
					{
						// Keep changed artifacts
						try
						{
							Artifact artifact = new Artifact( file, new URL( url ), container );
							if( artifact.isDifferent() )
							{
								container.getSincerity().getOut().println( "Keeping changed artifact: " + path );
								continue;
							}
						}
						catch( MalformedURLException x )
						{
							throw new SincerityException( "Could not parse artifacts configuration: " + file, x );
						}
					}

					container.getSincerity().getOut().println( "Deleting artifact: " + path );
					file.delete();
				}
			}
		}

		try
		{
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
		catch( IOException x )
		{
			throw new SincerityException( "Could not write artifacts configuration", x );
		}
	}
}

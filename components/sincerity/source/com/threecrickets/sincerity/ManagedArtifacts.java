package com.threecrickets.sincerity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.threecrickets.sincerity.exception.SincerityException;

public class ManagedArtifacts
{
	//
	// Construction
	//

	public ManagedArtifacts( File file, Container container )
	{
		this.file = file;
		this.container = container;
	}

	//
	// Attributes
	//

	public boolean wasInstalled( Artifact artifact ) throws SincerityException
	{
		validate();

		Object value = entries.get( artifact.getFile() );
		if( value != null )
			return new Entry( value ).wasInstalled();

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

	private Map<File, Entry> entries;

	private void validate() throws SincerityException
	{
		if( entries == null )
		{
			entries = new HashMap<File, Entry>();

			Properties properties = new Properties();
			try
			{
				FileInputStream stream = new FileInputStream( file );
				try
				{
					try
					{
						properties.load( stream );
						for( Map.Entry<Object, Object> entry : properties.entrySet() )
							entries.put( container.getAbsoluteFile( new File( entry.getKey().toString() ) ), new Entry( entry.getValue() ) );
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

	private void save() throws SincerityException
	{
		Properties properties = new Properties();
		for( Map.Entry<File, Entry> entry : entries.entrySet() )
			properties.put( container.getRelativePath( entry.getKey() ), entry.getValue().toString() );

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

	private void update( Iterable<Artifact> artifacts, int mode ) throws SincerityException
	{
		validate();

		// First mark all current artifacts as unnecessary
		for( Entry entry : entries.values() )
			if( !entry.isUnnecessary() )
				entry.setUnnecessary( true );

		// Add the new set of artifacts
		if( ( mode == MODE_ADD ) || ( mode == MODE_UPDATE ) )
			for( Artifact artifact : artifacts )
				entries.put( artifact.getFile(), new Entry( artifact ) );

		// Remove all unnecessary artifacts
		if( ( mode == MODE_CLEAN ) || ( mode == MODE_UPDATE ) )
		{
			for( Iterator<Map.Entry<File, Entry>> i = entries.entrySet().iterator(); i.hasNext(); )
			{
				Map.Entry<File, Entry> e = i.next();
				File file = e.getKey();
				Entry entry = e.getValue();

				if( entry.isUnnecessary() )
				{
					i.remove();

					file = container.getAbsoluteFile( file );

					if( !file.exists() )
						continue;

					if( entry.url != null )
					{
						// Keep changed artifacts
						if( new Artifact( file, entry.url, false, container ).isDifferent() )
						{
							container.getSincerity().getOut().println( "Keeping changed artifact: " + file );
							continue;
						}
					}

					container.getSincerity().getOut().println( "Deleting artifact: " + file );
					file.delete();
				}
			}
		}

		// Mark existing artifacts as installed
		for( Map.Entry<File, Entry> entry : entries.entrySet() )
			if( !entry.getValue().isUnnecessary() && !entry.getValue().wasInstalled() && entry.getKey().exists() )
				entry.getValue().setInstalled( true );

		save();
	}

	private static class Entry
	{
		public Entry( Artifact artifact )
		{
			url = artifact.getUrl();
		}

		public Entry( Object value ) throws SincerityException
		{
			String[] parsed = value.toString().split( ",", 2 );
			if( parsed.length < 1 )
				throw new SincerityException( "Could not parse artifacts configuration" );

			flags = Byte.valueOf( parsed[0] );
			if( parsed.length == 2 )
			{
				try
				{
					url = new URL( parsed[1] );
				}
				catch( MalformedURLException x )
				{
					throw new SincerityException( "Could not parse artifacts configuration", x );
				}
			}
			else
				url = null;
		}

		public final URL url;

		public boolean wasInstalled()
		{
			return ( flags & INSTALLED ) != 0;
		}

		public void setInstalled( boolean installed )
		{
			if( installed )
				flags |= INSTALLED;
			else
				flags ^= INSTALLED;
		}

		public boolean isUnnecessary()
		{
			return ( flags & UNNECESSARY ) != 0;
		}

		public void setUnnecessary( boolean unnecessary )
		{
			if( unnecessary )
				flags |= UNNECESSARY;
			else
				flags ^= UNNECESSARY;
		}

		@Override
		public String toString()
		{
			if( url == null )
				return Byte.toString( flags );
			else
				return Byte.toString( flags ) + "," + url;
		}

		private static final byte UNNECESSARY = 1 << 0;

		private static final byte INSTALLED = 1 << 1;

		private byte flags;
	}
}

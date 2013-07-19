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
import com.threecrickets.sincerity.internal.FileUtil;
import com.threecrickets.sincerity.internal.StringUtil;

/**
 * This class helps a {@link Dependencies} instance keep track of installed
 * artifacts.
 * <p>
 * Information about installed artifacts is stored in
 * "/configuration/sincerity/artifacts.conf".
 * 
 * @author Tal Liron
 */
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

		Entry entry = entries.get( artifact.getFile() );
		if( entry != null )
			return entry.wasInstalled();

		return false;
	}

	public byte[] getDigest( Artifact artifact ) throws SincerityException
	{
		validate();

		Entry entry = entries.get( artifact.getFile() );
		if( entry != null )
			return entry.digest;

		return null;
	}

	//
	// Operations
	//

	public void update( Iterable<Artifact> artifacts ) throws SincerityException
	{
		update( artifacts, MODE_UPDATE );
	}

	public void clean() throws SincerityException
	{
		update( null, MODE_CLEAN );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final static int MODE_UPDATE = 0;

	private final static int MODE_CLEAN = 1;

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

		// Merge in the new set of artifacts
		if( mode == MODE_UPDATE )
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
					{
						if( container.getSincerity().getVerbosity() >= 3 )
							container.getSincerity().getOut().println( "Artifact already deleted: " + file );
						continue;
					}

					if( ( entry.url != null ) && FileUtil.isUrlValid( entry.url ) )
					{
						// Keep changed artifacts
						try
						{
							byte[] digest = entry.digest;
							if( digest == null )
								digest = FileUtil.getDigest( entry.url.openStream() );

							if( !FileUtil.isSameContent( file, digest ) )
							{
								if( container.getSincerity().getVerbosity() >= 3 )
									container.getSincerity().getOut().println( "Keeping changed artifact: " + file );
								continue;
							}
						}
						catch( IOException x )
						{
							throw new SincerityException( "Could not compare digest for artifact: " + file, x );
						}
					}

					// Delete artifact
					if( container.getSincerity().getVerbosity() >= 3 )
						container.getSincerity().getOut().println( "Deleting artifact: " + file );
					if( !file.delete() )
						throw new SincerityException( "Could not delete artifact: " + file );
					try
					{
						FileUtil.deleteEmptyDirectoryRecursive( file.getParentFile() );
					}
					catch( IOException x )
					{
						throw new SincerityException( "Could not delete parent directories for artifact: " + file, x );
					}
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
		public Entry( Artifact artifact ) throws SincerityException
		{
			flags = 0;
			url = artifact.getUrl();
			if( url != null )
			{
				try
				{
					digest = FileUtil.getDigest( url.openStream() );
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not create artifacts configuration entry for: " + url, x );
				}
			}
			else
				digest = null;
		}

		public Entry( Object value ) throws SincerityException
		{
			String[] parsed = value.toString().split( ",", 2 );
			if( parsed.length < 1 )
				throw new SincerityException( "Could not parse artifacts configuration: " + value );

			flags = Byte.valueOf( parsed[0] );

			if( parsed.length >= 2 )
			{
				try
				{
					url = new URL( parsed[1] );
					if( parsed.length >= 3 )
						digest = StringUtil.fromHex( parsed[2] );
					else
						digest = null;
				}
				catch( MalformedURLException x )
				{
					throw new SincerityException( "Could not parse artifacts configuration: " + value, x );
				}
			}
			else
			{
				url = null;
				digest = null;
			}
		}

		public final URL url;

		public final byte[] digest;

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
			else if( digest == null )
				return Byte.toString( flags ) + "," + url;
			else
				return Byte.toString( flags ) + "," + url + "," + StringUtil.toHex( digest );
		}

		private static final byte UNNECESSARY = 1 << 0;

		private static final byte INSTALLED = 1 << 1;

		private byte flags;
	}
}

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.IoUtil;
import com.threecrickets.sincerity.internal.StringUtil;

/**
 * This class manages a database of artifacts for a a {@link Dependencies}
 * instance.
 * <p>
 * The database is normally stored in "/configuration/sincerity/artifacts.conf".
 * 
 * @author Tal Liron
 */
public class ManagedArtifacts
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param file
	 *        The database file (usually
	 *        "/configuration/sincerity/artifacts.conf")
	 * @param container
	 *        The container
	 */
	public ManagedArtifacts( File file, Container container )
	{
		this.file = file;
		this.container = container;
	}

	//
	// Attributes
	//

	/**
	 * True if the artifact was marked as installed.
	 * 
	 * @param artifact
	 *        The artifact
	 * @return True if was installed
	 * @throws SincerityException
	 */
	public boolean wasInstalled( Artifact artifact ) throws SincerityException
	{
		load();

		Entry entry = entries.get( artifact.getPath() );
		if( entry != null )
			return entry.wasInstalled();

		return false;
	}

	/**
	 * The digest when the artifact's file was first unpacked.
	 * 
	 * @param artifact
	 *        The artifact
	 * @return The digest or null
	 * @throws SincerityException
	 */
	public byte[] getOriginalDigest( Artifact artifact ) throws SincerityException
	{
		load();

		Entry entry = entries.get( artifact.getPath() );
		if( entry != null )
			return entry.originalDigest;

		return null;
	}

	//
	// Operations
	//

	/**
	 * Adds the artifact to the database, marking the configuration file as
	 * requiring a save if the addition caused a change.
	 * 
	 * @param artifact
	 * @param installed
	 * @throws SincerityException
	 */
	public void add( Artifact artifact, boolean installed ) throws SincerityException
	{
		load();

		String key = artifact.getPath();
		Entry entry = new Entry( artifact, installed );
		Entry existing = entries.get( key );
		if( ( existing != null ) && ( existing.equals( entry ) ) )
			return;

		changed = true;
		entries.put( key, entry );
	}

	/**
	 * Saves the database if there were changes.
	 * 
	 * @throws SincerityException
	 */
	public void save() throws SincerityException
	{
		if( !changed )
			return;

		Properties properties = new Properties();
		for( Map.Entry<String, Entry> entry : entries.entrySet() )
			properties.put( entry.getKey(), entry.getValue().toString() );

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

		changed = false;
	}

	/**
	 * Removes all artifacts from the database, deletes all unnecessary files
	 * that have not been modified (for which the digest has not changed), and
	 * finally saves the changes.
	 * 
	 * @throws SincerityException
	 */
	public void prune() throws SincerityException
	{
		prune( null );
	}

	/**
	 * Removes all artifacts from the database that are <i>not</i> mentioned,
	 * deletes all unnecessary files that have not been modified (for which the
	 * digest has not changed), and finally saves the changes.
	 * 
	 * @param necessaryArtifacts
	 *        The artifacts that should stay
	 * @throws SincerityException
	 */
	public void prune( Iterable<Artifact> necessaryArtifacts ) throws SincerityException
	{
		load();

		for( Iterator<Map.Entry<String, Entry>> i = entries.entrySet().iterator(); i.hasNext(); )
		{
			Map.Entry<String, Entry> e = i.next();
			String path = e.getKey();
			Entry entry = e.getValue();

			// Is the entry still necessary?
			boolean necessary = false;
			if( necessaryArtifacts != null )
			{
				for( Artifact necessaryArtifact : necessaryArtifacts )
				{
					if( path.equals( necessaryArtifact.getPath() ) )
					{
						necessary = true;
						break;
					}
				}
			}

			// Nope!
			if( !necessary )
			{
				i.remove();
				changed = true;

				File file = new File( container.getRoot(), path );

				if( !file.exists() )
				{
					if( container.getSincerity().getVerbosity() >= 3 )
						container.getSincerity().getOut().println( "Artifact doesn't exist, nothing to delete: " + file );
					continue;
				}

				// Keep changed artifacts
				if( entry.originalDigest != null )
				{
					try
					{
						if( !IoUtil.isSameContent( file, entry.originalDigest ) )
						{
							if( container.getSincerity().getVerbosity() >= 2 )
								container.getSincerity().getOut().println( "Not deleting unnecessary artifact because it has been changed: " + file );
							continue;
						}
					}
					catch( IOException x )
					{
						throw new SincerityException( "Could not compare digest for unnecessary artifact: " + file, x );
					}
				}

				// Delete artifact
				if( container.getSincerity().getVerbosity() >= 2 )
					container.getSincerity().getOut().println( "Deleting unnecessary artifact: " + file );
				if( !file.delete() )
					throw new SincerityException( "Could not delete unnecessary artifact: " + file );
				try
				{
					IoUtil.deleteEmptyDirectoryRecursive( file.getParentFile() );
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not delete parent directories for artifact: " + file, x );
				}
			}
		}

		save();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private final Container container;

	private boolean changed;

	private Map<String, Entry> entries;

	/**
	 * Loads and caches the database.
	 * 
	 * @throws SincerityException
	 */
	private void load() throws SincerityException
	{
		if( entries == null )
		{
			entries = new HashMap<String, Entry>();

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
							entries.put( entry.getKey().toString(), new Entry( entry.getValue() ) );
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

	/**
	 * Managed artifact entry in the database.
	 */
	private static class Entry
	{
		public Entry( Artifact artifact, boolean installed ) throws SincerityException
		{
			flags = installed ? INSTALLED : 0;
			try
			{
				originalDigest = artifact.getOriginDigest();
			}
			catch( IOException x )
			{
				throw new SincerityException( "Could not create artifacts configuration entry for: " + artifact.getOriginUrl(), x );
			}
		}

		public Entry( Object value ) throws SincerityException
		{
			String[] parsed = value.toString().split( ",", 2 );
			if( parsed.length < 1 )
				throw new SincerityException( "Could not parse artifacts configuration: " + value );

			flags = Byte.valueOf( parsed[0] );

			if( parsed.length >= 2 )
				originalDigest = StringUtil.fromHex( parsed[1] );
			else
				originalDigest = null;
		}

		public final byte[] originalDigest;

		public boolean wasInstalled()
		{
			return ( flags & INSTALLED ) != 0;
		}

		@Override
		public boolean equals( Object o )
		{
			if( !( o instanceof Entry ) )
				return false;
			Entry entry = (Entry) o;
			if( flags != entry.flags )
				return false;
			if( originalDigest == null )
			{
				if( entry.originalDigest != null )
					return false;
			}
			else if( !Arrays.equals( originalDigest, entry.originalDigest ) )
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			if( originalDigest == null )
				return Byte.toString( flags );
			else
				return Byte.toString( flags ) + "," + StringUtil.toHex( originalDigest );
		}

		private static final byte INSTALLED = 1;

		private byte flags;
	}
}

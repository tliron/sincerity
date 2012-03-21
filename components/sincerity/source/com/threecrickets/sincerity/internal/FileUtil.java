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

package com.threecrickets.sincerity.internal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileType;
import org.apache.commons.vfs.cache.DefaultFilesCache;
import org.apache.commons.vfs.impl.DefaultFileReplicator;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.bzip2.Bzip2FileProvider;
import org.apache.commons.vfs.provider.gzip.GzipFileProvider;
import org.apache.commons.vfs.provider.local.DefaultLocalFileProvider;
import org.apache.commons.vfs.provider.tar.TarFileProvider;
import org.apache.commons.vfs.provider.tar.Tbz2FileProvider;
import org.apache.commons.vfs.provider.tar.TgzFileProvider;
import org.apache.commons.vfs.provider.zip.ZipFileProvider;

public class FileUtil
{
	public static void unpack( File archiveFile, File destinationDir, File workdDir ) throws IOException
	{
		String scheme = null;
		String name = archiveFile.getName();
		DefaultFileSystemManager manager = new DefaultFileSystemManager();
		try
		{
			boolean untar = false;
			if( name.endsWith( ".zip" ) )
			{
				scheme = "zip:";
				manager.addProvider( "zip", new ZipFileProvider() );
			}
			else if( name.endsWith( ".tar.gz" ) || name.endsWith( ".tgz" ) )
			{
				scheme = "gz:";
				untar = true;
				manager.addProvider( "tar", new TarFileProvider() );
				manager.addProvider( "gz", new GzipFileProvider() );
				manager.addProvider( "tgz", new TgzFileProvider() );
			}
			else if( name.endsWith( ".tar.bz2" ) )
			{
				scheme = "bz2:";
				untar = true;
				manager.addProvider( "tar", new TarFileProvider() );
				manager.addProvider( "bz2", new Bzip2FileProvider() );
				manager.addProvider( "tbz2", new Tbz2FileProvider() );
			}

			if( scheme != null )
			{
				DefaultFileReplicator replicator = new DefaultFileReplicator( workdDir );
				replicator.init();
				manager.setReplicator( replicator );
				manager.setTemporaryFileStore( replicator );
				DefaultLocalFileProvider fileProvider = new DefaultLocalFileProvider();
				manager.addProvider( "file", fileProvider );
				manager.setDefaultProvider( fileProvider );
				manager.setFilesCache( new DefaultFilesCache() );
				manager.init();

				String path = scheme + archiveFile.toURI();
				FileObject fileObject = manager.resolveFile( path );
				FileObject[] children = fileObject.getChildren();
				if( untar && children.length > 0 )
				{
					FileObject tar = manager.resolveFile( new File( workdDir, children[0].getName().getBaseName() ).toURI().toString() );
					org.apache.commons.vfs.FileUtil.copyContent( children[0], tar );
					tar = manager.resolveFile( "tar:" + tar.getName() );
					children = tar.getChildren();
				}

				for( FileObject child : children )
					copyRecursive( manager, child, manager.resolveFile( destinationDir.toURI().toString() ) );
			}
		}
		finally
		{
			manager.close();
		}
	}

	public static final void copyRecursive( FileSystemManager manager, FileObject file, FileObject folder ) throws IOException
	{
		folder.createFolder();
		FileType type = file.getType();
		FileObject target = manager.resolveFile( folder + "/" + file.getName().getBaseName() );
		if( type == FileType.FILE )
			org.apache.commons.vfs.FileUtil.copyContent( file, target );
		else if( type == FileType.FOLDER )
		{
			for( FileObject child : file.getChildren() )
				copyRecursive( manager, child, target );
		}
	}

	public static final void copyRecursive( File fromDir, File toDir ) throws IOException
	{
		DefaultFileSystemManager manager = new DefaultFileSystemManager();
		try
		{
			DefaultLocalFileProvider fileProvider = new DefaultLocalFileProvider();
			manager.addProvider( "file", fileProvider );
			manager.setDefaultProvider( fileProvider );
			manager.setFilesCache( new DefaultFilesCache() );
			manager.init();
			copyRecursive( manager, manager.resolveFile( fromDir.toURI().toString() ), manager.resolveFile( toDir.toURI().toString() ) );
		}
		finally
		{
			manager.close();
		}
	}

	public static void deleteRecursive( File file ) throws IOException
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				deleteRecursive( child );
		if( !file.delete() )
			throw new IOException( "Could not delete file: " + file );
	}

	public static void listRecursiveEndsWith( File file, String postfix, Collection<File> files )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				listRecursiveEndsWith( child, postfix, files );
		else if( file.getName().endsWith( postfix ) )
			if( !files.contains( file ) )
				files.add( file );
	}

	public static boolean isSameContent( URL url, File file ) throws IOException
	{
		try
		{
			byte[] urlDigest = FileUtil.getDigest( url.openStream() );
			byte[] fileDigest = FileUtil.getDigest( new FileInputStream( file ) );
			return Arrays.equals( urlDigest, fileDigest );
		}
		catch( FileNotFoundException x )
		{
			return false;
		}
	}

	public static byte[] getDigest( InputStream stream ) throws IOException
	{
		try
		{
			MessageDigest md = MessageDigest.getInstance( "SHA-1" );
			md.reset();
			byte[] buf = new byte[BUFFER_SIZE];
			int len = 0;
			while( ( len = stream.read( buf ) ) != -1 )
				md.update( buf, 0, len );
			return md.digest();
		}
		catch( NoSuchAlgorithmException x )
		{
			IOException io = new IOException();
			io.initCause( x );
			throw io;
		}
		finally
		{
			stream.close();
		}
	}

	public static String[] separateExtensionFromFilename( String filename )
	{
		return filename.split( "\\.(?=[^\\.]+$)", 2 );
	}

	public static List<String> readLines( File file ) throws IOException
	{
		try
		{
			return readLines( new FileInputStream( file ) );
		}
		catch( FileNotFoundException x )
		{
			return new ArrayList<String>();
		}
	}

	public static List<String> readLines( InputStream stream ) throws IOException
	{
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader( new InputStreamReader( stream ) );
		try
		{
			String line;
			while( ( line = reader.readLine() ) != null )
				lines.add( line );
		}
		catch( IOException x )
		{
			throw x;
		}
		finally
		{
			try
			{
				reader.close();
			}
			catch( IOException x )
			{
			}
		}
		return lines;
	}

	public static void writeLines( File file, Iterable<String> lines ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( file ) );
		try
		{
			for( String line : lines )
			{
				writer.write( line );
				writer.write( '\n' );
			}
		}
		catch( IOException x )
		{
			throw x;
		}
		finally
		{
			try
			{
				writer.close();
			}
			catch( IOException x )
			{
			}
		}
	}

	public static final int BUFFER_SIZE = 2048;
}

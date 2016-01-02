/**
 * Copyright 2011-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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

import com.threecrickets.creel.util.DigestUtil;
import com.threecrickets.jvm.json.Json;
import com.threecrickets.jvm.json.JsonSyntaxError;

/**
 * File utilities.
 * 
 * @author Tal Liron
 */
public abstract class IoUtil
{
	//
	// Static operations
	//

	/**
	 * Unpacks all files in an archive using Apache Commons VFS.
	 * <p>
	 * Supported formats: zip, tar.gz/tgz, tar.bz2.
	 * 
	 * @param archiveFile
	 *        The archive file
	 * @param destinationDir
	 *        The destination directory
	 * @param workDir
	 *        The work directory
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void unpack( File archiveFile, File destinationDir, File workDir ) throws IOException
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
				DefaultFileReplicator replicator = new DefaultFileReplicator( workDir );
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
					FileObject tar = manager.resolveFile( new File( workDir, children[0].getName().getBaseName() ).toURI().toString() );
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

	/**
	 * Copies a file or a complete subdirectory tree using Apache Commons VFS.
	 * 
	 * @param manager
	 *        The file system manager
	 * @param file
	 *        The source file or directory
	 * @param folder
	 *        The target folder
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copyRecursive( FileSystemManager manager, FileObject file, FileObject folder ) throws IOException
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

	/**
	 * Copies a complete subdirectory tree using Apache Commons VFS.
	 * 
	 * @param fromDir
	 *        The source directory
	 * @param toDir
	 *        The target directory
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void copyRecursive( File fromDir, File toDir ) throws IOException
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

	/**
	 * Deletes an empty directory, including all parent directories that are
	 * also empty, stopping at the first non-empty parent.
	 * 
	 * @param directory
	 *        The start directory
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void deleteEmptyDirectoryRecursive( File directory ) throws IOException
	{
		if( ( directory != null ) && directory.isDirectory() )
		{
			File[] files = directory.listFiles();
			if( ( files == null ) || ( files.length == 0 ) )
			{
				if( !directory.delete() )
					throw new IOException( "Could not delete empty directory: " + directory );
				deleteEmptyDirectoryRecursive( directory.getParentFile() );
			}
		}
	}

	/**
	 * Deletes a file or a subdirectory tree.
	 * 
	 * @param file
	 *        The file or directory
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void deleteRecursive( File file ) throws IOException
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				deleteRecursive( child );
		if( !file.delete() )
			throw new IOException( "Could not delete file: " + file );
	}

	/**
	 * Recursively gathers all files with filenames ending with a postfix.
	 * 
	 * @param file
	 *        The file or directory
	 * @param postfix
	 *        The required postfix
	 * @param files
	 *        The collection to which we will add files
	 */
	public static void listRecursiveEndsWith( File file, String postfix, Collection<File> files )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				listRecursiveEndsWith( child, postfix, files );
		else if( file.getName().endsWith( postfix ) )
			if( !files.contains( file ) )
				files.add( file );
	}

	/**
	 * True if the file has a specific digest.
	 * 
	 * @param file
	 *        The file
	 * @param digest
	 *        The digest
	 * @param algorithm
	 *        The algorithm
	 * @return True if the digests are equal
	 * @throws IOException
	 *         In case of an I/O error
	 * @see DigestUtil
	 */
	public static boolean isSameContent( File file, byte[] digest, String algorithm ) throws IOException
	{
		try
		{
			byte[] fileDigest = DigestUtil.getDigest( file, algorithm );
			return Arrays.equals( fileDigest, digest );
		}
		catch( FileNotFoundException x )
		{
			return false;
		}
	}

	/**
	 * Returns an array of 2 strings, the first being the filename without its
	 * extension, the second being its extension.
	 * 
	 * @param filename
	 *        The filename
	 * @return The two elements of the filename
	 */
	public static String[] separateExtensionFromFilename( String filename )
	{
		return filename.split( "\\.(?=[^\\.]+$)", 2 );
	}

	/**
	 * Reads all lines in a file using UTF-8.
	 * <p>
	 * Note that this method returns an empty list if the file doesn't exist.
	 * 
	 * @param file
	 *        The file
	 * @return The lines
	 * @throws IOException
	 *         In case of an I/O error
	 */
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

	/**
	 * Reads all lines in a stream using UTF-8. The stream will be consumed and
	 * closed.
	 * 
	 * @param stream
	 *        The stream
	 * @return The lines
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static List<String> readLines( InputStream stream ) throws IOException
	{
		ArrayList<String> lines = new ArrayList<String>();
		BufferedReader reader = new BufferedReader( new InputStreamReader( stream, StandardCharsets.UTF_8 ), BUFFER_SIZE );
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
				stream.close();
			}
			catch( IOException x )
			{
			}
		}
		return lines;
	}

	/**
	 * Decodes a JSON UTF-8 file into JVM primitives and collection/map
	 * instances.
	 * <p>
	 * Note that this method returns null if the file doesn't exist.
	 * 
	 * @param file
	 *        The file
	 * @return The decoded JSON
	 * @throws IOException
	 *         In case of an I/O error
	 * @throws JsonSyntaxError
	 *         In case of a JSON syntax error
	 */
	public static Object readJson( File file ) throws IOException, JsonSyntaxError
	{
		String content = com.threecrickets.creel.util.IoUtil.readText( file );
		return content != null ? Json.from( content ) : null;
	}

	/**
	 * Writes lines to a file using UTF-8, overwriting its current contents if
	 * it has any.
	 * <p>
	 * Lines end in a newline character.
	 * 
	 * @param file
	 *        The file
	 * @param lines
	 *        The lines
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void writeLines( File file, Iterable<String> lines ) throws IOException
	{
		// Possible bug?
		// See:
		// http://tripoverit.blogspot.com/2007/04/javas-utf-8-and-unicode-writing-is.html

		FileOutputStream stream = new FileOutputStream( file );
		BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( stream, StandardCharsets.UTF_8 ), BUFFER_SIZE );
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
				stream.close();
			}
			catch( IOException x )
			{
			}
		}
	}

	/**
	 * Writes JVM primitives and collection/map instances to a JSON UTF-8 file.
	 * 
	 * @param file
	 *        The file
	 * @param object
	 *        The object (JVM primitives and collection instances)
	 * @param expand
	 *        Whether to expand the JSON with newlines, indents, and spaces
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void writeJson( File file, Object object, boolean expand ) throws IOException
	{
		String content = Json.to( object, expand ).toString();
		FileOutputStream stream = new FileOutputStream( file );
		PrintWriter writer = new PrintWriter( new BufferedWriter( new OutputStreamWriter( stream, StandardCharsets.UTF_8 ), BUFFER_SIZE ) );
		try
		{
			writer.write( content );
		}
		finally
		{
			writer.close();
		}
	}

	/**
	 * Updates a file's last-modified timestamp.
	 * <p>
	 * If the file doesn't exist, it is created as an empty file, including
	 * necessary parent directories.
	 * 
	 * @param file
	 *        The file
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static void touch( File file ) throws IOException
	{
		if( file.exists() )
			file.setLastModified( System.currentTimeMillis() );
		else
		{
			File parent = file.getParentFile();
			if( parent != null )
				parent.mkdirs();
			file.createNewFile();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final int BUFFER_SIZE = 16 * 1024;

	private IoUtil()
	{
	}
}

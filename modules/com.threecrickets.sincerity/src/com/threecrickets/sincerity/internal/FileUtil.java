package com.threecrickets.sincerity.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

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
	public static boolean unpack( File archiveFile, File destinationDir, File workdDir )
	{
		try
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

					copyRecursive( manager, children, manager.resolveFile( destinationDir.toURI().toString() ) );

					return true;
				}
			}
			finally
			{
				manager.close();
			}
		}
		catch( Exception x )
		{
			x.printStackTrace();
		}

		return false;
	}

	public static final void copyRecursive( FileSystemManager manager, FileObject[] files, FileObject folder ) throws IOException
	{
		for( FileObject file : files )
		{
			folder.createFolder();
			FileType type = file.getType();
			FileObject target = manager.resolveFile( folder + "/" + file.getName().getBaseName() );
			if( type == FileType.FILE )
				org.apache.commons.vfs.FileUtil.copyContent( file, target );
			else if( type == FileType.FOLDER )
				copyRecursive( manager, file.getChildren(), target );
		}
	}

	public static boolean isSameContent( URL url, File file ) throws IOException
	{
		byte[] urlDigest = FileUtil.getDigest( url.openStream() );
		byte[] fileDigest = FileUtil.getDigest( new FileInputStream( file ) );
		return Arrays.equals( urlDigest, fileDigest );
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

	public static final int BUFFER_SIZE = 2048;
}

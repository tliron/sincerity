package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.FileUtil;

public class Artifact
{
	//
	// Construction
	//

	public Artifact( File file, URL url, Container container )
	{
		this.file = file;
		this.url = url;
		path = container.getRelativePath( file );
	}

	//
	// Attributes
	//

	public File getFile()
	{
		return file;
	}

	public String getPath()
	{
		return path;
	}

	public URL getUrl()
	{
		return url;
	}

	public boolean isDifferent() throws SincerityException
	{
		try
		{
			return !FileUtil.isSameContent( url, file );
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not compare artifact " + file + " to " + url, x );
		}
	}

	//
	// Operations
	//

	public void unpack( boolean overwrite ) throws SincerityException
	{
		if( file.exists() )
		{
			// TODO: back up in cache if overwriting!!!

			if( overwrite )
				System.out.println( "Installing artifact (overwriting): " + path );
			else
			{
				System.out.println( "Not installing modified artifact: " + path );
				return;
			}
		}
		else
			System.out.println( "Installing artifact: " + path );

		try
		{
			org.apache.ivy.util.FileUtil.copy( url, file, null );
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not copy artifact from " + url + " to " + file, x );
		}
	}

	//
	// Object
	//

	@Override
	public int hashCode()
	{
		return file.hashCode();
	}

	@Override
	public boolean equals( Object o )
	{
		if( this == o )
			return true;
		if( !( o instanceof Artifact ) )
			return false;
		return file.equals( ( (Artifact) o ).file );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private final URL url;

	private final String path;
}

package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.threecrickets.sincerity.exception.UnpackingException;
import com.threecrickets.sincerity.internal.FileUtil;

public class Artifact implements Comparable<Artifact>
{
	//
	// Construction
	//

	public Artifact( File file, URL url, boolean isVolatile, Container container )
	{
		this.file = file;
		this.url = url;
		this.isVolatile = isVolatile;
		this.container = container;
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

	public boolean isVolatile()
	{
		return isVolatile;
	}

	public boolean isDifferent() throws UnpackingException
	{
		try
		{
			return !FileUtil.isSameContent( url, file );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not compare artifact " + file + " to " + url, x );
		}
	}

	//
	// Operations
	//

	public void install( String filter, boolean overwrite ) throws UnpackingException
	{
		if( file.exists() )
		{
			// TODO: back up in cache if overwriting!!!

			if( overwrite )
			{
				if( container.getSincerity().getVerbosity() >= 2 )
					container.getSincerity().getOut().println( "Installing artifact (overwriting): " + path );
			}
			else
			{
				if( container.getSincerity().getVerbosity() >= 2 )
					container.getSincerity().getOut().println( "Not installing modified artifact: " + path );
				return;
			}
		}
		else
		{
			if( container.getSincerity().getVerbosity() >= 2 )
				container.getSincerity().getOut().println( "Installing artifact: " + path );
		}

		try
		{
			org.apache.ivy.util.FileUtil.copy( url, file, null );
		}
		catch( IOException x )
		{
			throw new UnpackingException( "Could not copy artifact from " + url + " to " + file, x );
		}
	}

	//
	// Comparable
	//

	public int compareTo( Artifact o )
	{
		return file.compareTo( o.file );
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

	private final boolean isVolatile;

	private final Container container;

	private final String path;
}

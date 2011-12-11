package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.URL;

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

	public URL getUrl()
	{
		return url;
	}

	public boolean isDifferent() throws IOException
	{
		return !FileUtil.isSameContent( url, file );
	}

	//
	// Operations
	//

	public void unpack( boolean overwrite ) throws IOException
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

		org.apache.ivy.util.FileUtil.copy( url, file, null );
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

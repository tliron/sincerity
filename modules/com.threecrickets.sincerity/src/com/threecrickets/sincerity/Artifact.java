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

	public Artifact( File file, URL url )
	{
		this.file = file.getAbsoluteFile();
		this.url = url;
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
			if( overwrite )
				System.out.println( "Overwriting file: " + file );
			else
			{
				System.out.println( "Keeping existing file: " + file );
				return;
			}
		}
		else
			System.out.println( "Writing file: " + file );

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
}

package com.threecrickets.sincerity.ivy.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.ivy.plugins.repository.BasicResource;

/**
 * Unused.
 */
public class StringResource extends BasicResource
{
	//
	// Construction
	//

	public StringResource( String name, String content )
	{
		super( name, true, content.length(), System.currentTimeMillis(), true );
		this.content = content;
	}

	//
	// Resource
	//

	@Override
	public InputStream openStream() throws IOException
	{
		return new ByteArrayInputStream( content.getBytes() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String content;
}

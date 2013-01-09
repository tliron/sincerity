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

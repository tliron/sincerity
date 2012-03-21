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

package com.threecrickets.sincerity.ivy.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ivy.plugins.repository.BasicResource;

/**
 * Unused.
 */
public class ReferenceResource extends BasicResource
{
	//
	// Construction
	//

	public ReferenceResource( String name, Object reference )
	{
		super( name, true, 0, System.currentTimeMillis(), true );
		this.reference = reference;
	}

	//
	// Attributes
	//

	public Object getReference()
	{
		return reference;
	}

	@Override
	public InputStream openStream() throws IOException
	{
		System.out.println( "..." );
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Object reference;
}

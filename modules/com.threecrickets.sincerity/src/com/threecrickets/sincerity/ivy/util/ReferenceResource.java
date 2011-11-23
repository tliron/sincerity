package com.threecrickets.sincerity.ivy.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.ivy.plugins.repository.BasicResource;

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

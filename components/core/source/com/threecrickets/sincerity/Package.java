package com.threecrickets.sincerity;

import java.util.ArrayList;

import com.threecrickets.sincerity.exception.UnpackingException;

public class Package extends ArrayList<Artifact>
{
	//
	// Operations
	//

	public void unpack( String filter, boolean overwrite ) throws UnpackingException
	{
		for( Artifact artifact : this )
			artifact.unpack( filter, overwrite );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
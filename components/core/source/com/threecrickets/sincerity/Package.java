package com.threecrickets.sincerity;

import java.util.ArrayList;

import com.threecrickets.sincerity.exception.SincerityException;

public class Package extends ArrayList<Artifact>
{
	//
	// Operations
	//

	public void unpack( boolean overwrite ) throws SincerityException
	{
		for( Artifact artifact : this )
			artifact.unpack( overwrite );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
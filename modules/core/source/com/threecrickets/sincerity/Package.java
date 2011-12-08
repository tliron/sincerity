package com.threecrickets.sincerity;

import java.io.IOException;
import java.util.ArrayList;

public class Package extends ArrayList<Artifact>
{
	//
	// Operations
	//

	public void unpack( boolean overwrite ) throws IOException
	{
		for( Artifact artifact : this )
			artifact.unpack( overwrite );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
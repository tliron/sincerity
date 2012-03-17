package com.threecrickets.sincerity;

import java.io.File;

public class Template
{
	//
	// Construction
	//

	public Template( File root )
	{
		this.root = root;
	}

	//
	// Attributes
	//

	public final File root;

	public String getName()
	{
		return root.getName();
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return getName();
	}
}

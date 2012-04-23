package com.threecrickets.sincerity.eclipse.internal;

import org.eclipse.osgi.util.NLS;

public abstract class Messages extends NLS
{
	//
	// Constants
	//

	public static String DirLabel;

	public static String PageDesc;

	public static String NoHome;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	static
	{
		NLS.initializeMessages( Messages.class.getPackage().getName() + ".messages", Messages.class );
	}

	private Messages()
	{
	}
}

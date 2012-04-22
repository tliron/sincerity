package com.threecrickets.sincerity.eclipse;

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

	private static final String BUNDLE_NAME = "com.threecrickets.sincerity.eclipse.messages";

	static
	{
		NLS.initializeMessages( BUNDLE_NAME, Messages.class );
	}

	private Messages()
	{
	}
}

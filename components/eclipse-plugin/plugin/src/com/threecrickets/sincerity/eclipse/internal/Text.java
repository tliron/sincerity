package com.threecrickets.sincerity.eclipse.internal;

import org.eclipse.osgi.util.NLS;

public abstract class Text extends NLS
{
	//
	// Constants
	//

	public static String PreferencesInstallation;

	public static String PreferencesUseExternalSincerity;

	public static String PreferencesExternalSincerity;

	public static String PreferencesJre;

	public static String PreferencesUseExternalJre;

	public static String PreferencesExternalJre;

	public static String ClasspathName;

	public static String ClasspathDescription;

	public static String SharedClasspathName;

	public static String SharedClasspathDescription;

	public static String LaunchProgramOrUri;

	public static String LaunchProgramOrUriError;

	public static String NoSincerity;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	static
	{
		NLS.initializeMessages( Text.class.getPackage().getName() + ".text", Text.class );
	}

	private Text()
	{
	}
}

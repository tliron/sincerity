package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;

public class SincerityPreferencesInitializer extends AbstractPreferenceInitializer
{
	//
	// AbstractPreferenceInitializer
	//

	@Override
	public void initializeDefaultPreferences()
	{
		String home = System.getProperty( HOME_PROPERTY );
		if( home == null )
			home = System.getenv( HOME_VARIABLE );
		if( home != null )
			SincerityPlugin.getDefault().getPreferenceStore().setDefault( SincerityPlugin.EXTERNAL_SINCERITY_ATTRIBUTE, home );

		if( Platform.getBundle( SincerityPlugin.INTERNAL_INSTALLATION_BUNDLE ) == null )
			SincerityPlugin.getDefault().getPreferenceStore().setValue( SincerityPlugin.USE_EXTERNAL_SINCERITY_ATTRIBUTE, true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String HOME_PROPERTY = "sincerity.home";

	private static final String HOME_VARIABLE = "SINCERITY_HOME";
}

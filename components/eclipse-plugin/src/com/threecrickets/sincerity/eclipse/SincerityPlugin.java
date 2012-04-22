package com.threecrickets.sincerity.eclipse;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SincerityPlugin extends AbstractUIPlugin
{
	//
	// Constants
	//

	public static final String ID = "com.threecrickets.sincerity";

	public static final String SINCERITY_HOME = "SINCERITY_HOME";

	//
	// Static attributes
	//

	public static SincerityPlugin getDefault()
	{
		return plugin;
	}

	public static SimpleLog getSimpleLog()
	{
		if( log == null )
			log = new SimpleLog( ID );
		return log;
	}

	//
	// Attributes
	//

	public File getSincerityHome()
	{
		String home = getPreferenceStore().getString( SINCERITY_HOME );
		File homeDir = home == null ? null : new File( home );
		if( ( homeDir == null ) || !homeDir.isDirectory() )
		{
			MessageDialog.openInformation( null, "Sincerity", Messages.NoHome );
			return null;
		}
		return homeDir;
	}

	//
	// AbstractUIPlugin
	//

	@Override
	public void start( BundleContext context ) throws Exception
	{
		super.start( context );
		plugin = this;
		getSimpleLog().log( IStatus.INFO, "Sincerity plugin started" );
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		plugin = null;
		super.stop( context );
		getSimpleLog().log( IStatus.INFO, "Sincerity plugin stopped" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static SincerityPlugin plugin;

	private static SimpleLog log;
}

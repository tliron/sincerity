package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class SincerityPlugin extends AbstractUIPlugin
{
	//
	// Constants
	//

	public static final String ID = "com.threecrickets.sincerity";

	//
	// Static attributes
	//

	public static SimpleLog getSimpleLog()
	{
		if( log == null )
			log = new SimpleLog( ID );
		return log;
	}

	public static SincerityPlugin getDefault()
	{
		return plugin;
	}

	//
	// Construction
	//

	public SincerityPlugin()
	{
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

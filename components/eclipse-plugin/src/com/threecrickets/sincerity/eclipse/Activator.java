package com.threecrickets.sincerity.eclipse;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class Activator extends AbstractUIPlugin
{
	//
	// Constants
	//

	public static final String ID = "com.threecrickets.sincerity";

	//
	// Construction
	//

	public Activator()
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
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ SINCERITY ACTIVATOR");
	}

	@Override
	public void stop( BundleContext context ) throws Exception
	{
		plugin = null;
		super.stop( context );
	}

	public static Activator getDefault()
	{
		return plugin;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static Activator plugin;
}

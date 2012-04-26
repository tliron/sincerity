package com.threecrickets.sincerity.eclipse;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import com.threecrickets.sincerity.eclipse.internal.SimpleLog;

public class SincerityPlugin extends AbstractUIPlugin
{
	//
	// Constants
	//

	public static final String ID = "com.threecrickets.sincerity.plugin";

	public static final String USE_EXTERNAL_SINCERITY_ATTRIBUTE = "USE_EXTERNAL_SINCERITY";

	public static final String EXTERNAL_SINCERITY_ATTRIBUTE = "EXTERNAL_SINCERITY";

	public static final String USE_EXTERNAL_JRE_ATTRIBUTE = "USE_EXTERNAL_JRE";

	public static final String EXTERNAL_JRE_ATTRIBUTE = "EXTERNAL_JRE";

	public static final String INTERNAL_INSTALLATION_BUNDLE = "com.threecrickets.sincerity";

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

	public File getSincerityRoot()
	{
		File homeDir = null;

		if( getPreferenceStore().getBoolean( USE_EXTERNAL_SINCERITY_ATTRIBUTE ) )
		{
			String home = getPreferenceStore().getString( EXTERNAL_SINCERITY_ATTRIBUTE );
			homeDir = home == null ? null : new File( home );
		}
		else
		{
			try
			{
				Bundle bundle = Platform.getBundle( INTERNAL_INSTALLATION_BUNDLE );
				URL url = FileLocator.find( bundle, new Path( "/content/" ), null );
				url = FileLocator.resolve( url );
				homeDir = new File( url.getPath() );
			}
			catch( Exception x )
			{
				SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
			}
		}

		if( ( homeDir == null ) || !homeDir.isDirectory() )
		{
			// MessageDialog.openInformation( null, "Sincerity",
			// Text.NoSincerity );
			return null;
		}

		SincerityPlugin.getSimpleLog().log( IStatus.INFO, "Using Sincerity at: " + homeDir );
		return homeDir;
	}

	public boolean getUseAlternateJre()
	{
		return getPreferenceStore().getBoolean( USE_EXTERNAL_JRE_ATTRIBUTE );
	}

	public IVMInstall getAlternateJre()
	{
		String id = getPreferenceStore().getString( EXTERNAL_JRE_ATTRIBUTE );
		return JavaRuntime.getVMFromCompositeId( id );
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

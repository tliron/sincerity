package com.threecrickets.sincerity.eclipse;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.threecrickets.sincerity.eclipse.internal.Text;
import com.threecrickets.sincerity.eclipse.internal.SimpleLog;

public class SincerityPlugin extends AbstractUIPlugin
{
	//
	// Constants
	//

	public static final String ID = "com.threecrickets.sincerity";

	public static final String SINCERITY_HOME_ATTRIBUTE = "SINCERITY_HOME";

	public static final String USE_EXTERNAL_JRE_ATTRIBUTE = "USE_EXTERNAL_JRE";

	public static final String EXTERNAL_JRE_ATTRIBUTE = "EXTERNAL_JRE";

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
		String home = getPreferenceStore().getString( SINCERITY_HOME_ATTRIBUTE );
		File homeDir = home == null ? null : new File( home );
		if( ( homeDir == null ) || !homeDir.isDirectory() )
		{
			MessageDialog.openInformation( null, "Sincerity", Text.NoHome );
			return null;
		}
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

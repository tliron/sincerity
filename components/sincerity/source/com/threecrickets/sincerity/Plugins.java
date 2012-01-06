package com.threecrickets.sincerity;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.threecrickets.scripturian.internal.ServiceLoader;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.FileUtil;

public class Plugins extends AbstractMap<String, Plugin>
{
	//
	// Construction
	//

	public Plugins( Sincerity sincerity ) throws SincerityException
	{
		super();

		ClassLoader classLoader;
		try
		{

			classLoader = sincerity.getContainer().getBootstrap();

			// Scripturian plugins
			File pluginsDir = sincerity.getContainer().getLibrariesFile( "scripturian", "plugins" );
			if( pluginsDir.isDirectory() )
			{
				ScripturianShell shell = new ScripturianShell( sincerity.getContainer(), null, true );
				for( String pluginFilename : pluginsDir.list() )
				{
					try
					{
						String pluginName = FileUtil.separateExtensionFromFilename( pluginFilename )[0];
						Plugin plugin = new DelegatedPlugin( pluginName, shell );
						plugins.put( plugin.getName(), plugin );
					}
					catch( Exception x )
					{
						x.printStackTrace( sincerity.getErr() );
					}
				}
			}
		}
		catch( NoContainerException x )
		{
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		this.classLoader = classLoader;

		// JVM plugins
		for( Plugin plugin : ServiceLoader.load( Plugin.class, classLoader ) )
			plugins.put( plugin.getName(), plugin );
	}

	//
	// Attributes
	//

	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	//
	// AbstractMap
	//

	@Override
	public Set<Map.Entry<String, Plugin>> entrySet()
	{
		return Collections.unmodifiableSet( plugins.entrySet() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ClassLoader classLoader;

	private HashMap<String, Plugin> plugins = new HashMap<String, Plugin>();
}

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

			classLoader = sincerity.getContainer().getDependencies().getClassLoader();

			// Scripturian plugins
			File pluginsDir = sincerity.getContainer().getLibrariesFile( "plugins" );
			if( pluginsDir.isDirectory() )
			{
				ScripturianShell shell = new ScripturianShell( sincerity.getContainer(), pluginsDir, true );
				for( String pluginFile : pluginsDir.list() )
				{
					try
					{
						Plugin plugin = new DelegatedPlugin( pluginFile, shell );
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

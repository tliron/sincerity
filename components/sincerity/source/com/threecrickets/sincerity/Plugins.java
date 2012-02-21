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

public class Plugins extends AbstractMap<String, Plugin1>
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
			Container container = sincerity.getContainer();
			classLoader = container.getBootstrap();

			// Scripturian plugins
			File pluginsDir = container.getLibrariesFile( "scripturian", "plugins" );
			if( pluginsDir.isDirectory() )
			{
				ScripturianShell shell = new ScripturianShell( container, null, true );
				for( File pluginFile : pluginsDir.listFiles() )
				{
					if( pluginFile.isHidden() )
						continue;

					try
					{
						Plugin1 plugin = new DelegatedPlugin( pluginFile, shell );
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

		// JVM plugins
		for( Plugin1 plugin : ServiceLoader.load( Plugin1.class, classLoader ) )
			plugins.put( plugin.getName(), plugin );
	}

	//
	// AbstractMap
	//

	@Override
	public Set<Map.Entry<String, Plugin1>> entrySet()
	{
		return Collections.unmodifiableSet( plugins.entrySet() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private HashMap<String, Plugin1> plugins = new HashMap<String, Plugin1>();
}

package com.threecrickets.sincerity;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentFileSource;
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
				System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, sincerity.getContainer().getCacheFile().getPath() );
				ParsingContext parsingContext = new ParsingContext();
				parsingContext.setLanguageManager( new LanguageManager() );
				parsingContext.setDocumentSource( new DocumentFileSource<Executable>( pluginsDir, "default", "js", 1000 ) );
				parsingContext.setPrepare( true );

				for( String pluginFile : pluginsDir.list() )
				{
					try
					{
						Plugin plugin = new ScripturianPlugin( pluginFile, parsingContext, sincerity );
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

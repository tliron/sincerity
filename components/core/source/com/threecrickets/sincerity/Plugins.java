package com.threecrickets.sincerity;

import java.io.File;
import java.util.HashMap;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.internal.ServiceLoader;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;

public class Plugins extends HashMap<String, Plugin>
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
			File pluginsDir = new File( sincerity.getContainer().getRoot(), "plugins" );
			if( pluginsDir.isDirectory() )
			{
				System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, new File( sincerity.getContainer().getRoot(), "cache" ).getAbsolutePath() );
				ParsingContext parsingContext = new ParsingContext();
				parsingContext.setLanguageManager( new LanguageManager() );
				parsingContext.setDocumentSource( new DocumentFileSource<Executable>( pluginsDir, "default", "js", 1000 ) );
				parsingContext.setPrepare( true );

				for( String pluginFile : pluginsDir.list() )
				{
					try
					{
						Plugin plugin = new ScripturianPlugin( pluginFile, parsingContext, sincerity );
						put( plugin.getName(), plugin );
					}
					catch( Exception x )
					{
						x.printStackTrace();
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
			put( plugin.getName(), plugin );
	}

	//
	// Attributes
	//

	public ClassLoader getClassLoader()
	{
		return classLoader;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final ClassLoader classLoader;
}

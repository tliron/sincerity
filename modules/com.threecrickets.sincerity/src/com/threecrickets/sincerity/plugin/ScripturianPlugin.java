package com.threecrickets.sincerity.plugin;

import java.io.File;

import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;

public class ScripturianPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "scripturian";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"execute"
		};
	}

	public void run( String command, String[] arguments, Sincerity sincerity ) throws Exception
	{
		if( "execute".equals( command ) )
		{
			if( arguments.length < 1 )
			{
				System.err.println( "'" + command + "' command requires: [uri] ..." );
				return;
			}

			System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, new File( sincerity.getContainer().getRoot(), "cache" ).getPath() );
			MainPlugin.main( sincerity, "com.threecrickets.scripturian.Scripturian", arguments );
		}
		else
			throw new Exception( "Unknown command: " + command );
	}
}

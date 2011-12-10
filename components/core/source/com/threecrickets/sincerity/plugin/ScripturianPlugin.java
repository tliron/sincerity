package com.threecrickets.sincerity.plugin;

import java.io.File;

import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.Main;
import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.SincerityExecutionController;

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

	public void run( Command command ) throws Exception
	{
		String name = command.getName();
		if( "execute".equals( name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "uri" );

			System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, new File( command.getSincerity().getContainer().getRoot(), "cache" ).getAbsolutePath() );
			Main main = new Main( arguments );
			main.setExecutionController( new SincerityExecutionController( command.getSincerity() ) );
			main.run();

			// MainPlugin.main( sincerity,
			// "com.threecrickets.scripturian.Scripturian", arguments );
		}
		else
			throw new UnknownCommandException( command );
	}
}

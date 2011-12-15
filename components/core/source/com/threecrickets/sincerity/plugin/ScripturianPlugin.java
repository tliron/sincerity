package com.threecrickets.sincerity.plugin;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.Main;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
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

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "execute".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "uri" );

			System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, command.getSincerity().getContainer().getFile( "cache" ).getPath() );

			Main main = new Main( arguments );
			main.setSource( new DocumentFileSource<Executable>( command.getSincerity().getContainer().getRoot(), "default", "js", -1 ) );
			main.getLibrarySources().add( new DocumentFileSource<Executable>( command.getSincerity().getContainer().getFile( "libraries" ), "default", "js", -1 ) );
			main.setExecutionController( new SincerityExecutionController( command.getSincerity() ) );
			main.run();

			// MainPlugin.main( sincerity,
			// "com.threecrickets.scripturian.Scripturian", arguments );
		}
		else
			throw new UnknownCommandException( command );
	}
}

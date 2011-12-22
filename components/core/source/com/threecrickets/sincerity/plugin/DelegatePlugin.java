package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.Main;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.ClassUtil;
import com.threecrickets.sincerity.internal.Pipe;
import com.threecrickets.sincerity.internal.ProcessDestroyer;
import com.threecrickets.sincerity.internal.SincerityExecutionController;
import com.threecrickets.sincerity.internal.StringUtil;

public class DelegatePlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "delegate";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"main", "start", "execute"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "main".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "main class name" );

			ClassUtil.main( command.getSincerity(), arguments );
		}
		else if( "start".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "uri" );

			Container container = command.getSincerity().getContainer();
			System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, container.getFile( "cache" ).getPath() );

			if( !arguments[0].startsWith( "/" ) )
				arguments[0] = "/programs/" + arguments[0];

			Main scripturian = new Main( new LanguageManager( container.getDependencies().getClassLoader() ), container.getRoot(), true, arguments[0], "default", "js", "document", "application", command.getSincerity()
				.getOut(), command.getSincerity().getErr(), arguments );
			scripturian.getLibrarySources().add( new DocumentFileSource<Executable>( container.getFile( "libraries" ), "default", "js", -1 ) );
			scripturian.setExecutionController( new SincerityExecutionController( command.getSincerity() ) );
			scripturian.run();

			// MainPlugin.main( sincerity,
			// "com.threecrickets.scripturian.Scripturian", arguments );
		}
		else if( "execute".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command );

			try
			{
				boolean block = false;
				if( "--block".equals( arguments[0] ) )
				{
					block = true;
					String[] newArguments = new String[arguments.length - 1];
					System.arraycopy( arguments, 1, newArguments, 0, newArguments.length );
					arguments = newArguments;
				}

				File executable = command.getSincerity().getContainer().getExecutablesFile( arguments[0] );
				if( executable.exists() )
					arguments[0] = executable.getPath();

				Runtime runtime = Runtime.getRuntime();
				Process process = runtime.exec( arguments );
				if( !block )
					runtime.addShutdownHook( new ProcessDestroyer( process ) );
				new Thread( new Pipe( new InputStreamReader( process.getInputStream() ), command.getSincerity().getOut() ) ).start();
				new Thread( new Pipe( new InputStreamReader( process.getErrorStream() ), command.getSincerity().getErr() ) ).start();
				if( block )
				{
					try
					{
						process.waitFor();
					}
					catch( InterruptedException x )
					{
						throw new SincerityException( "System command execution was interrupted: " + StringUtil.join( arguments, " " ), x );
					}
				}
			}
			catch( IOException x )
			{
				x.printStackTrace();
				throw new SincerityException( "Error executing system command: " + StringUtil.join( arguments, " " ), x );
			}
		}
		else
			throw new UnknownCommandException( command );
	}
}

package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.threecrickets.scripturian.LanguageAdapter;
import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.ScripturianShell;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.ClassUtil;
import com.threecrickets.sincerity.internal.Pipe;
import com.threecrickets.sincerity.internal.ProcessDestroyer;
import com.threecrickets.sincerity.internal.StringUtil;

public class DelegatePlugin implements Plugin1
{
	//
	// Plugin
	//

	public int getVersion()
	{
		return 1;
	}

	public String getName()
	{
		return "delegate";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"main", "start", "execute", "languages"
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

			if( !arguments[0].startsWith( "/" ) )
				arguments[0] = "/programs/" + arguments[0];

			ScripturianShell shell = new ScripturianShell( command.getSincerity().getContainer(), null, true, arguments );
			shell.execute( arguments[0] );
		}
		else if( "execute".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command );

			try
			{
				boolean background = false;
				if( "--background".equals( arguments[0] ) )
				{
					background = true;
					String[] newArguments = new String[arguments.length - 1];
					System.arraycopy( arguments, 1, newArguments, 0, newArguments.length );
					arguments = newArguments;
				}

				File executable = command.getSincerity().getContainer().getExecutablesFile( arguments[0] );
				if( executable.exists() )
					arguments[0] = executable.getPath();

				ProcessBuilder processBuilder = new ProcessBuilder( arguments );
				Map<String, String> environment = processBuilder.environment();
				String path = environment.get( "PATH" );
				String sincerityPath = command.getSincerity().getContainer().getExecutablesFile().getPath();
				if( path != null )
					environment.put( "PATH", sincerityPath + File.pathSeparator + path );
				else
					environment.put( "PATH", sincerityPath );
				Process process = processBuilder.start();

				if( !background )
					Runtime.getRuntime().addShutdownHook( new ProcessDestroyer( process ) );

				new Thread( new Pipe( new InputStreamReader( process.getInputStream() ), command.getSincerity().getOut() ) ).start();
				new Thread( new Pipe( new InputStreamReader( process.getErrorStream() ), command.getSincerity().getErr() ) ).start();

				if( !background )
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
				x.printStackTrace( command.getSincerity().getErr() );
				throw new SincerityException( "Error executing system command: " + StringUtil.join( arguments, " " ), x );
			}
		}
		else if( "languages".equals( commandName ) )
		{
			ScripturianShell shell = new ScripturianShell( command.getSincerity().getContainer(), null, true );
			for( LanguageAdapter languageAdapter : shell.getLanguageManager().getAdapters() )
				command.getSincerity().getOut().println( languageAdapter.getAttributes().get( "name" ) );
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
	}
}

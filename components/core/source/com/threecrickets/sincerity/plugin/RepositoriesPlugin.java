package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Repositories;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class RepositoriesPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "repositories";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"attach", "detach"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "attach".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 3 )
				throw new BadArgumentsCommandException( command, "section", "name", "type" );

			String section = arguments[0];
			String name = arguments[1];
			String type = arguments[2];

			if( "maven".equals( type ) || "ibiblio".equals( type ) )
			{
				if( arguments.length < 4 )
					throw new BadArgumentsCommandException( command, "section", "name", "type", "url" );

				String url = arguments[3];

				Repositories repositories = command.getSincerity().getContainer().getRepositories();
				if( !repositories.addMaven( section, name, url ) )
					command.getSincerity().getErr().println( "Repository already in use: " + section + ":" + name );
			}
			else if( "pypi".equals( type ) || "python".equals( type ) )
			{
				if( arguments.length < 4 )
					throw new BadArgumentsCommandException( command, "section", "name", "type", "url" );

				String url = arguments[3];

				Repositories repositories = command.getSincerity().getContainer().getRepositories();
				if( !repositories.addPyPi( section, name, url ) )
					command.getSincerity().getErr().println( "Repository already in use: " + section + ":" + name );
			}
			else
				command.getSincerity().getErr().println( "Unknown repository type: " + type );
		}
		else if( "detach".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 2 )
				throw new BadArgumentsCommandException( command, "section", "name" );

			String section = arguments[0];
			String name = arguments[1];

			Repositories repositories = command.getSincerity().getContainer().getRepositories();
			if( !repositories.remove( section, name ) )
				command.getSincerity().getErr().println( "Repository was not in use: " + section + ":" + name );
		}
		else
			throw new UnknownCommandException( command );
	}
}

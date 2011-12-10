package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Repositories;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
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

	public void run( Command command ) throws Exception
	{
		String name = command.getName();
		if( "attach".equals( name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 3 )
				throw new BadArgumentsCommandException( command, "section", "name", "type" );

			String section = arguments[0];
			String repositoryName = arguments[1];
			String type = arguments[2];

			if( "maven".equals( type ) || "ibiblio".equals( type ) )
			{
				if( arguments.length < 4 )
					throw new BadArgumentsCommandException( command, "section", "repositoryName", "type", "url" );

				String url = arguments[3];

				Repositories repositories = command.getSincerity().getContainer().getRepositories();
				if( !repositories.addIbiblio( section, repositoryName, url ) )
					System.err.println( "Repository already in use: " + section + ":" + repositoryName );
			}
			else if( "pypi".equals( type ) || "python".equals( type ) )
			{
				if( arguments.length < 4 )
					throw new BadArgumentsCommandException( command, "section", "repositoryName", "type", "url" );

				String url = arguments[3];

				Repositories repositories = command.getSincerity().getContainer().getRepositories();
				if( !repositories.addPyPi( section, repositoryName, url ) )
					System.err.println( "Repository already in use: " + section + ":" + repositoryName );
			}
			else
				System.err.println( "Unknown repository type: " + type );
		}
		else if( "detach".equals( name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 2 )
				throw new BadArgumentsCommandException( command, "section", "name" );

			String section = arguments[0];
			String repositoryName = arguments[1];

			Repositories repositories = command.getSincerity().getContainer().getRepositories();
			if( !repositories.remove( section, repositoryName ) )
				System.err.println( "Repository was not in use: " + section + ":" + repositoryName );
		}
		else
			throw new UnknownCommandException( command );
	}
}

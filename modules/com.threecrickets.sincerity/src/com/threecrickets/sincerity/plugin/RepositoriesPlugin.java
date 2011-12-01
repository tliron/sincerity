package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Repositories;
import com.threecrickets.sincerity.Sincerity;

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

	public void run( String command, String[] arguments, Sincerity sincerity ) throws Exception
	{
		Repositories repositories = sincerity.getContainer().getRepositories();

		if( "attach".equals( command ) )
		{
			if( arguments.length < 3 )
				throw new Exception( "'" + command + "' command requires: [section] [name] [type] ..." );

			String section = arguments[0];
			String name = arguments[1];
			String type = arguments[2];

			if( "maven".equals( type ) || "ibiblio".equals( type ) )
			{
				if( arguments.length < 4 )
					throw new Exception( "'" + command + " [section] [name] " + type + "' command also requires: [url]" );

				String url = arguments[3];

				if( !repositories.addIbiblio( section, name, url ) )
					System.err.println( "Repository already in use: " + section + ":" + name );
			}
			else if( "pypi".equals( type ) || "python".equals( type ) )
			{
				if( arguments.length < 4 )
					throw new Exception( "'" + command + " [section] [name] " + type + "' command also requires: [url]" );

				String url = arguments[3];

				if( !repositories.addPyPi( section, name, url ) )
					System.err.println( "Repository already in use: " + section + ":" + name );
			}
			else
				System.err.println( "Unknown repository type: " + type );
		}
		else if( "detach".equals( command ) )
		{
			if( arguments.length < 2 )
				throw new Exception( "'" + command + "' command requires: [section] [name]" );

			String section = arguments[0];
			String name = arguments[1];

			if( !repositories.remove( section, name ) )
				System.err.println( "Repository was not in use: " + section + ":" + name );
		}
		else
			throw new Exception( "Unknown command: " + command );
	}
}

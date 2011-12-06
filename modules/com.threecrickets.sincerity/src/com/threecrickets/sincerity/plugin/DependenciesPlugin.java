package com.threecrickets.sincerity.plugin;

import java.io.OutputStreamWriter;

import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Package;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;

public class DependenciesPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "dependencies";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"dependencies", "install", "unpack", "reset", "add", "remove"
		};
	}

	public void run( String command, String[] arguments, Sincerity sincerity ) throws Exception
	{
		Dependencies dependencies = sincerity.getContainer().getDependencies();

		if( "dependencies".equals( command ) )
		{
			dependencies.getResolvedDependencies().printTree( new OutputStreamWriter( System.out ) );
		}
		else if( "install".equals( command ) )
		{
			boolean overwrite = "true".equals( sincerity.getProperties().get( "overwrite" ) );

			dependencies.install( overwrite );
		}
		else if( "unpack".equals( command ) )
		{
			String name;
			if( arguments.length < 1 )
				name = null;
			else
				name = arguments[0];

			boolean overwrite = "true".equals( sincerity.getProperties().get( "overwrite" ) );

			if( name == null )
				dependencies.getPackages().unpack( overwrite );
			else
			{
				Package pack = dependencies.getPackages().get( name );
				if( pack == null )
					throw new Exception( "Unknown package: " + name );
				pack.unpack( overwrite );
			}
		}
		else if( "reset".equals( command ) )
		{
			dependencies.reset();
		}
		else if( "add".equals( command ) )
		{
			if( arguments.length < 2 )
				throw new Exception( "'" + command + "' command requires: [group] [name] [[version]]" );

			String organisation = arguments[0];
			String name = arguments[1];
			String revision;
			if( arguments.length < 3 )
				revision = "latest.integration";
			else
				revision = arguments[2];

			if( !dependencies.add( organisation, name, revision ) )
				System.err.println( "Dependency already in container: " + organisation + ":" + name + ":" + revision );
		}
		else if( "remove".equals( command ) )
		{
			if( arguments.length < 3 )
				throw new Exception( "'" + command + "' command requires: [group] [name] [version]" );

			String organisation = arguments[0];
			String name = arguments[1];
			String revision = arguments[2];

			if( !dependencies.remove( organisation, name, revision ) )
				System.err.println( "Dependency was not in container: " + organisation + ":" + name + ":" + revision );
		}
		else
			throw new Exception( "Unknown command: " + command );
	}
}

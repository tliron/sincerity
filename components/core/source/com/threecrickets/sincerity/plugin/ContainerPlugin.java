package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class ContainerPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "container";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"create", "use"
		};
	}

	public void run( Command command ) throws Exception
	{
		String name = command.getName();
		if( "create".equals( name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path", "[template]" );

			String containerLocation = arguments[0];
			String template;
			if( arguments.length < 2 )
				template = "default";
			else
				template = arguments[1];

			System.out.println( template );

			command.getSincerity().setContainer( containerLocation );
		}
		else if( "use".equals( name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path" );

			command.getSincerity().setContainer( arguments[0] );
		}
		else
			throw new UnknownCommandException( command );
	}
}

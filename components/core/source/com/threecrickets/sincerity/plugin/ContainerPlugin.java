package com.threecrickets.sincerity.plugin;

import java.io.File;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
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
			"create", "use", "clone"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "create".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path", "[template]" );

			File containerRoot = new File( arguments[0] );
			String template;
			if( arguments.length < 2 )
				template = "default";
			else
				template = arguments[1];
			File templateDir = new File( new File( command.getSincerity().getHome(), "templates" ), template );

			command.getSincerity().createContainer( containerRoot, templateDir );
		}
		else if( "use".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path" );

			File containerRoot = new File( arguments[0] );
			command.getSincerity().setContainerRoot( containerRoot );
		}
		else if( "clone".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "target container root path" );

			File containerRoot = new File( arguments[0] );
			command.getSincerity().createContainer( containerRoot, command.getSincerity().getContainer().getRoot() );
		}
		else
			throw new UnknownCommandException( command );
	}
}

package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.io.IOException;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.FileUtil;

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

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "create".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path", "[template]" );

			String containerLocation = arguments[0];

			File containerRootDir = new File( containerLocation );
			if( containerRootDir.exists() )
			{
				if( new File( containerRootDir, Container.SINCERITY_DIR ).exists() )
					throw new SincerityException( "The path is already a Sincerity container: " + containerRootDir );
			}

			String template;
			if( arguments.length < 2 )
				template = "default";
			else
				template = arguments[1];
			File templateDir = new File( command.getSincerity().getHome(), "templates/" + template );
			if( !templateDir.isDirectory() )
				throw new SincerityException( "Could not find container template: " + templateDir );

			containerRootDir.mkdirs();
			new File( containerRootDir, Container.SINCERITY_DIR ).mkdirs();
			for( File file : templateDir.listFiles() )
			{
				try
				{
					FileUtil.copyRecursive( file, containerRootDir );
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not copy file from template to container: " + file );
				}
			}

			command.getSincerity().setContainer( containerLocation );
		}
		else if( "use".equals( commandName ) )
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

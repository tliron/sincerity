package com.threecrickets.sincerity.plugin;

import java.io.File;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
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

	public void run( Command command ) throws Exception
	{
		String commandName = command.getName();
		if( "create".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path", "[template]" );

			String containerLocation = arguments[0];

			File containerRootDir = new File( containerLocation ).getCanonicalFile();
			if( containerRootDir.exists() )
				throw new Exception( "Container root path already exists: " + containerRootDir );

			String template;
			if( arguments.length < 2 )
				template = "default";
			else
				template = arguments[1];
			File templateDir = new File( command.getSincerity().getHome(), "templates/" + template );
			if( !templateDir.isDirectory() )
				throw new Exception( "Could not find container template: " + templateDir );

			containerRootDir.mkdirs();
			new File( containerRootDir, Container.SINCERITY_DIR_NAME ).mkdirs();
			for (File file : templateDir.listFiles() )
				FileUtil.copyRecursive( file, containerRootDir );

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

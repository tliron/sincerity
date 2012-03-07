package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.io.IOException;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.FileUtil;

public class ContainerPlugin implements Plugin1
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
		return "container";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"create", "use", "clone", "clean"
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

			// TODO: look for templates according to ~/.sincerity/sincerity.conf
			// first (likely ~/.sincerity/templates
			// same for 'templatize'

			command.getSincerity().createContainer( containerRoot, templateDir );
		}
		else if( "use".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path" );

			File containerRoot = new File( arguments[0] );
			if( !containerRoot.isDirectory() )
				throw new NoContainerException( "Specified container root path is not a directory: " + containerRoot );

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
		else if( "clean".equals( commandName ) )
		{
			Container container = command.getSincerity().getContainer();
			Dependencies dependencies = container.getDependencies();
			dependencies.uninstall();

			File cache = container.getFile( "cache" );
			if( cache.isDirectory() )
			{
				try
				{
					FileUtil.deleteRecursive( cache );
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not clean cache", x );
				}
			}
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
	}
}

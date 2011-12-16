package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.io.IOException;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.FileUtil;

public class TemplatesPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "templates";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"templates", "templatize"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "templates".equals( commandName ) )
		{
			File templatesDir = new File( command.getSincerity().getHome(), "templates" );
			if( templatesDir.isDirectory() )
			{
				for( File templateDir : templatesDir.listFiles() )
				{
					if( templateDir.isDirectory() )
						System.out.println( templateDir.getName() );
				}
			}
		}
		else if( "templatize".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "template name" );

			String template = arguments[0];
			File sincerityHome = command.getSincerity().getHome();
			File containerRoot = command.getSincerity().getContainer().getRoot();

			File templateDir = new File( new File( sincerityHome, "templates" ), template );
			if( templateDir.exists() )
				throw new SincerityException( "The template already exists: " + templateDir );

			templateDir.mkdirs();
			for( File file : containerRoot.listFiles() )
			{
				try
				{
					FileUtil.copyRecursive( file, templateDir );
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not create template: " + templateDir, x );
				}
			}
		}
		else
			throw new UnknownCommandException( command );
	}
}

/**
 * Copyright 2011-2014 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.io.IOException;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.Template;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.IoUtil;
import com.threecrickets.sincerity.plugin.gui.TemplatesPane;

/**
 * The templates plugin supports the following commands:
 * <ul>
 * <li><b>templates</b>: prints a list of all available templates in this
 * Sincerity installation.</li>
 * <li><b>templatize</b>: turns this container into a template. The first
 * argument is the name of the template to use. The entire container is copied
 * as is, so you may want to clean it out first.</li>
 * </ul>
 * Additionally, this plugin adds a "Templates" tab to the GUI.
 * 
 * @author Tal Liron
 * @see Template
 * @see TemplatesPane
 */
public class TemplatesPlugin implements Plugin1
{
	//
	// Plugin
	//

	public int getInterfaceVersion()
	{
		return 1;
	}

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
			for( Template template : command.getSincerity().getTemplates() )
				command.getSincerity().getOut().println( template );
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
					IoUtil.copyRecursive( file, templateDir );
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

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		sincerity.getFrame().getTabs().add( "Templates", new TemplatesPane( sincerity ) );
	}
}

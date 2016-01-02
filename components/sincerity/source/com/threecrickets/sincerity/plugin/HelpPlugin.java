/**
 * Copyright 2011-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin;

import java.io.PrintWriter;
import java.util.Map;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.swing.CommandsPane;

/**
 * The help plugin supports the following commands:
 * <ul>
 * <li><b>version</b>: prints the Sincerity version.</li>
 * <li><b>help</b>: prints a list of all available commands, organized by
 * plugin. Note that this command can either run with a container or without
 * one.</li>
 * </ul>
 * Additionally, this plugin adds a "Commands" tab to the GUI.
 * 
 * @author Tal Liron
 * @see CommandsPane
 */
public class HelpPlugin implements Plugin1
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
		return "help";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"version", "help", "verbosity"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		Sincerity sincerity = command.getSincerity();
		PrintWriter out = sincerity.getOut();

		if( "version".equals( commandName ) )
		{
			Map<String, String> version = sincerity.getVersion();
			for( Map.Entry<String, String> entry : version.entrySet() )
				out.println( entry.getKey() + "=" + entry.getValue() );
		}
		else if( "help".equals( commandName ) )
		{
			for( Plugin1 plugin : sincerity.getPlugins().values() )
				for( String pluginCommand : plugin.getCommands() )
					out.println( plugin.getName() + Command.PLUGIN_COMMAND_SEPARATOR + pluginCommand );
		}
		else if( "verbosity".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				out.println( sincerity.getVerbosity() );
			else
			{
				int verbosity = Integer.parseInt( arguments[0] );
				sincerity.setVerbosity( verbosity );
			}
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		sincerity.getFrame().getTabs().add( "Commands", new CommandsPane( sincerity ) );
	}
}

package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class HelpPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "help";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"help"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "help".equals( commandName ) )
		{
			for( Plugin plugin : command.getSincerity().getPlugins().values() )
				for( String pluginCommand : plugin.getCommands() )
					System.out.println( plugin.getName() + Command.PLUGIN_COMMAND_SEPARATOR + pluginCommand );
		}
		else
			throw new UnknownCommandException( command );
	}
}

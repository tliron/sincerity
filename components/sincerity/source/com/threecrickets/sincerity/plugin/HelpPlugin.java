package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.gui.ActionsPane;

public class HelpPlugin implements Plugin1
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
			for( Plugin1 plugin : command.getSincerity().getPlugins().values() )
				for( String pluginCommand : plugin.getCommands() )
					System.out.println( plugin.getName() + Command.PLUGIN_COMMAND_SEPARATOR + pluginCommand );
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		sincerity.getFrame().getPane().add( "Help", new ActionsPane( sincerity ) );
	}
}

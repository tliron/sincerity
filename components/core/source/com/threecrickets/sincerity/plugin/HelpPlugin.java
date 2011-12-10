package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;
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

	public void run( Command command, Sincerity sincerity ) throws Exception
	{
		if( "help".equals( command.name ) )
		{
			for( Plugin plugin : sincerity.getPlugins().values() )
				for( String pluginCommand : plugin.getCommands() )
					System.out.println( plugin.getName() + ":" + pluginCommand );
		}
		else
			throw new UnknownCommandException( command );
	}
}

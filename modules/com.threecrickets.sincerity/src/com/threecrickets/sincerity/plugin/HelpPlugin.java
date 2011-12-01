package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;

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

	public void run( String command, String[] arguments, Sincerity sincerity ) throws Exception
	{
		if( "help".equals( command ) )
		{
			for( Plugin plugin : sincerity.getPlugins() )
				for( String pluginCommand : plugin.getCommands() )
					System.out.println( plugin.getName() + ":" + pluginCommand );
		}
		else
			throw new Exception( "Unknown command: " + command );
	}
}

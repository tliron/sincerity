package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class ShortcutsPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "aliases";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"shortcuts"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "shortcuts".equals( commandName ) )
		{
			Shortcuts shortcuts = command.getSincerity().getContainer().getShortcuts();
			for( String shortcut : shortcuts )
			{
				System.out.print( shortcut );
				System.out.print( " =" );
				for( String item : shortcuts.get( shortcut ) )
				{
					System.out.print( ' ' );
					System.out.print( item );
				}
				System.out.println();
			}
		}
		else
			throw new UnknownCommandException( command );
	}
}

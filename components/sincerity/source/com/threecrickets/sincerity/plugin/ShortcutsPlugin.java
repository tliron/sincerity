/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.gui.ShortcutsPane;

public class ShortcutsPlugin implements Plugin1
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

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		sincerity.getFrame().getPane().add( "Shortcuts", new ShortcutsPane( sincerity ) );
	}
}

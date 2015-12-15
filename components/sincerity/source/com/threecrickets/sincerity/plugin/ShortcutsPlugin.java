/**
 * Copyright 2011-2015 Three Crickets LLC.
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

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.swing.ShortcutsPane;

/**
 * The shorcuts plugin supports the following commands:
 * <ul>
 * <li><b>shortcuts</b>: prints a list of all available shortcuts in this
 * container.</li>
 * </ul>
 * Additionally, this plugin adds a "Shortcuts" tab to the GUI.
 * 
 * @author Tal Liron
 * @see ShortcutsPane
 */
public class ShortcutsPlugin implements Plugin1
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
		return "shortcuts";
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
			Sincerity sincerity = command.getSincerity();
			Container<?, ?> container = sincerity.getContainer();
			Shortcuts shortcuts = container.getShortcuts();
			PrintWriter out = sincerity.getOut();
			for( String shortcut : shortcuts )
			{
				out.print( shortcut );
				out.print( " =" );
				for( String item : shortcuts.get( shortcut ) )
				{
					out.print( ' ' );
					out.print( item );
				}
				out.println();
			}
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		sincerity.getFrame().getTabs().add( "Shortcuts", new ShortcutsPane( sincerity ) );
	}
}

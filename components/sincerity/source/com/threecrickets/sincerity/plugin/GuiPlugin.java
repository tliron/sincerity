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
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.gui.Console;
import com.threecrickets.sincerity.plugin.gui.Frame;
import com.threecrickets.sincerity.plugin.gui.Splash;
import com.threecrickets.sincerity.plugin.gui.internal.GuiUtil;

/**
 * The GUI plugin supports the following commands:
 * <ul>
 * <li><b>gui</b>: starts the Sincerity GUI, using all available plugins. Note
 * that this command can either run with a container or without one, in which
 * case it would prompt the user to create a new container. Use the --ui=
 * property to change the Look-and-Feel. Some options are "native", "nimbus",
 * "metal", "gtk+" and "cde/motif".</li>
 * </ul>
 * 
 * @author Tal Liron
 * @see Plugin1#gui(Command)
 */
public class GuiPlugin implements Plugin1
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
		return "gui";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"gui"
		};
	}

	public void run( final Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "gui".equals( commandName ) )
		{
			// Don't show GUI while console is up
			if( Console.getCurrent() != null )
				return;

			command.setParse( true );

			String ui = command.getProperties().get( "ui" );
			if( ui == null )
				ui = "native";
			GuiUtil.initLookAndFeel( ui );

			new Splash( new Runnable()
			{
				public void run()
				{
					try
					{
						Sincerity sincerity = command.getSincerity();
						Frame frame = sincerity.getFrame();
						if( frame != null )
							frame.dispose();
						frame = new Frame( sincerity );
						sincerity.setFrame( frame );

						for( Plugin1 plugin : sincerity.getPlugins().values() )
						{
							try
							{
								plugin.gui( command );
							}
							catch( NoContainerException x )
							{
							}
						}

						frame.run();
					}
					catch( SincerityException x )
					{
						GuiUtil.error( x );
					}
				}
			} );
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
	}
}

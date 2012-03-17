package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.gui.Frame;
import com.threecrickets.sincerity.plugin.gui.GuiUtil;

public class GuiPlugin implements Plugin1
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
		return "gui";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"gui"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "gui".equals( commandName ) )
		{
			command.setParse( true );

			boolean isNative = command.getSwitches().contains( "native" );
			GuiUtil.setNativeLookAndFeel( isNative );

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
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
	}
}

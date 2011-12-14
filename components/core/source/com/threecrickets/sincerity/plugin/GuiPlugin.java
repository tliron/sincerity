package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.gui.Frame;
import com.threecrickets.sincerity.plugin.gui.GuiUtil;

public class GuiPlugin implements Plugin
{
	//
	// Plugin
	//

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
			GuiUtil.setNativeLookAndFeel();
			Frame frame = new Frame( command.getSincerity() );
			frame.setVisible( true );
		}
		else
			throw new UnknownCommandException( command );
	}
}

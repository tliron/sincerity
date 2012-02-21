package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Packages;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class PackagesPlugin implements Plugin1
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
		return "packages";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"unpack"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "unpack".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			String filter;
			if( arguments.length < 1 )
				filter = null;
			else
				filter = arguments[0];

			command.setParse( true );
			boolean overwrite = command.getSwitches().contains( "overwrite" );

			Packages packages = command.getSincerity().getContainer().getDependencies().getPackages();
			packages.install( filter, overwrite );
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
	}
}

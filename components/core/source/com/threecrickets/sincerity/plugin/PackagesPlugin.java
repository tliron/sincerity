package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Package;
import com.threecrickets.sincerity.Packages;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class PackagesPlugin implements Plugin
{
	//
	// Plugin
	//

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

	public void run( Command command ) throws Exception
	{
		String name = command.getName();
		if( "unpack".equals( name ) )
		{
			String[] arguments = command.getArguments();
			String packageName;
			if( arguments.length < 1 )
				packageName = null;
			else
				packageName = arguments[0];

			boolean overwrite = command.getSwitches().contains( "overwrite" );

			Packages packages = command.getSincerity().getContainer().getDependencies().getPackages();
			if( packageName == null )
				packages.unpack( overwrite );
			else
			{
				Package pack = packages.get( packageName );
				if( pack == null )
					throw new Exception( "Unknown package: " + packageName );
				pack.unpack( overwrite );
			}
		}
		else
			throw new UnknownCommandException( command );
	}
}

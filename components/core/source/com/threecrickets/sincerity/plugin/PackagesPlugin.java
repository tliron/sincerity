package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Package;
import com.threecrickets.sincerity.Packages;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;
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

	public void run( Command command, Sincerity sincerity ) throws Exception
	{
		Packages packages = sincerity.getContainer().getDependencies().getPackages();

		if( "unpack".equals( command.name ) )
		{
			String[] arguments = command.getArguments();
			String name;
			if( arguments.length < 1 )
				name = null;
			else
				name = arguments[0];

			boolean overwrite = command.getSwitches().contains( "overwrite" );

			if( name == null )
				packages.unpack( overwrite );
			else
			{
				Package pack = packages.get( name );
				if( pack == null )
					throw new Exception( "Unknown package: " + name );
				pack.unpack( overwrite );
			}
		}
		else
			throw new UnknownCommandException( command );
	}
}

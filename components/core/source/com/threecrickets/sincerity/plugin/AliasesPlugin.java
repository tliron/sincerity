package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Aliases;
import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class AliasesPlugin implements Plugin
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
			"aliases"
		};
	}

	public void run( Command command ) throws Exception
	{
		String name = command.getName();
		if( "aliases".equals( name ) )
		{
			Aliases aliases = command.getSincerity().getContainer().getAliases();
			for( String alias : aliases )
			{
				System.out.print( alias );
				System.out.print( " =" );
				for( String a : aliases.get( alias ) )
				{
					System.out.print( ' ' );
					System.out.print( a );
				}
				System.out.println();
			}
		}
		else
			throw new UnknownCommandException( command );
	}
}

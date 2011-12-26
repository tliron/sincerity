package com.threecrickets.sincerity.exception;

import java.util.Iterator;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;

public class AmbiguousCommandException extends CommandException
{
	//
	// Construction
	//

	public AmbiguousCommandException( Command command, Iterable<Plugin> plugins )
	{
		super( command, createMessage( command, plugins ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private static String createMessage( Command command, Iterable<Plugin> plugins )
	{
		StringBuilder s = new StringBuilder( "Ambiguous command: " );
		for( Iterator<Plugin> i = plugins.iterator(); i.hasNext(); )
		{
			Plugin plugin = i.next();
			s.append( plugin.getName() );
			s.append( Command.PLUGIN_COMMAND_SEPARATOR );
			s.append( command.getName() );
			if( i.hasNext() )
				s.append( ", " );
		}
		return s.toString();
	}
}

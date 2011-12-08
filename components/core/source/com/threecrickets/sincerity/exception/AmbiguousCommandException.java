package com.threecrickets.sincerity.exception;

import java.util.Iterator;

import com.threecrickets.sincerity.Plugin;

public class AmbiguousCommandException extends CommandException
{
	//
	// Construction
	//

	public AmbiguousCommandException( String command, Iterable<Plugin> plugins )
	{
		super( command, createMessage( command, plugins ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private static String createMessage( String command, Iterable<Plugin> plugins )
	{
		StringBuilder s = new StringBuilder( "Ambiguous command: " );
		for( Iterator<Plugin> i = plugins.iterator(); i.hasNext(); )
		{
			Plugin plugin = i.next();
			s.append( plugin.getName() );
			s.append( ':' );
			s.append( command );
			if( i.hasNext() )
				s.append( ", " );
		}
		return s.toString();
	}
}

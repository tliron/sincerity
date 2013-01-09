/**
 * Copyright 2011-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.exception;

import java.util.Iterator;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;

/**
 * Signifies that a Sincerity command is implemented by more than one plugin,
 * and thus it's unclear which version of the command is being referred to.
 * <p>
 * This problem can usually be solved by prefixing the plugin name to the
 * command.
 * 
 * @author Tal Liron
 */
public class AmbiguousCommandException extends CommandException
{
	//
	// Construction
	//

	public AmbiguousCommandException( Command command, Iterable<Plugin1> plugins )
	{
		super( command, createMessage( command, plugins ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private static String createMessage( Command command, Iterable<Plugin1> plugins )
	{
		StringBuilder s = new StringBuilder( "Ambiguous command: " );
		for( Iterator<Plugin1> i = plugins.iterator(); i.hasNext(); )
		{
			Plugin1 plugin = i.next();
			s.append( plugin.getName() );
			s.append( Command.PLUGIN_COMMAND_SEPARATOR );
			s.append( command.getName() );
			if( i.hasNext() )
				s.append( ", " );
		}
		return s.toString();
	}
}

/**
 * Copyright 2011-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Plugins;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;

import jline.console.completer.Completer;

/**
 * A <a href="https://github.com/jline/jline2">JLine</a> completer that
 * recognizes Sincerity commands.
 * 
 * @author Tal Liron
 */
public class CommandCompleter implements Completer
{
	//
	// Construction
	//

	public CommandCompleter()
	{
		this( "" );
	}

	public CommandCompleter( String prefix )
	{
		this.prefix = prefix;
	}

	//
	// Completer
	//

	@Override
	public int complete( String buffer, int cursor, List<CharSequence> candidates )
	{
		if( buffer == null )
			candidates.addAll( getCommands() );
		else
		{
			for( String match : getCommands().tailSet( buffer ) )
			{
				if( !match.startsWith( buffer ) || buffer.equals( match ) )
					break;
				candidates.add( match );
			}
		}

		if( candidates.size() == 1 )
			candidates.set( 0, candidates.get( 0 ) + " " );

		return candidates.isEmpty() ? -1 : 0;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String prefix;

	private SortedSet<String> getCommands()
	{
		TreeSet<String> commands = new TreeSet<String>();
		try
		{
			Plugins plugins = Sincerity.getCurrent().getPlugins();
			ArrayList<String> removes = new ArrayList<String>();
			for( Map.Entry<String, Plugin1> entry : plugins.entrySet() )
			{
				commands.add( prefix + entry.getKey() + Command.PLUGIN_COMMAND_SEPARATOR );
				for( String command : entry.getValue().getCommands() )
				{
					commands.add( prefix + entry.getKey() + Command.PLUGIN_COMMAND_SEPARATOR + command );
					if( commands.contains( prefix + command ) )
						// This command is not unique, so it can't be called
						// without the group prefix
						removes.add( prefix + command );
					commands.add( prefix + command );
				}
			}
			commands.removeAll( removes );
		}
		catch( SincerityException x )
		{
		}
		return commands;
	}
}

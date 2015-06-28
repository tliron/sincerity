/**
 * Copyright 2011-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin.console;

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
	// Completer
	//

	@Override
	public int complete( String buffer, int cursor, List<CharSequence> candidates )
	{
		if( buffer == null )
			candidates.addAll( getStrings() );
		else
		{
			for( String match : getStrings().tailSet( buffer ) )
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

	private SortedSet<String> getStrings()
	{
		TreeSet<String> strings = new TreeSet<String>();
		try
		{
			Plugins plugins = Sincerity.getCurrent().getContainer().getPlugins();
			ArrayList<String> removes = new ArrayList<String>();
			for( Map.Entry<String, Plugin1> entry : plugins.entrySet() )
			{
				strings.add( entry.getKey() + Command.PLUGIN_COMMAND_SEPARATOR );
				for( String command : entry.getValue().getCommands() )
				{
					strings.add( entry.getKey() + Command.PLUGIN_COMMAND_SEPARATOR + command );
					if( strings.contains( command ) )
						// This command is not unique, so it can't be called
						// without the group prefix
						removes.add( command );
					strings.add( command );
				}
			}
			strings.removeAll( removes );
		}
		catch( SincerityException x )
		{
		}
		return strings;
	}
}

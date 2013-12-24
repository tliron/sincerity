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

package com.threecrickets.sincerity.plugin;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

/**
 * The packages plugin supports the following commands:
 * <ul>
 * <li><b>unpack</b>: unpacks all packages and runs their installation hooks.
 * This allows you to run only this particular phase in the install process, in
 * case you do not want a full install.</li>
 * </ul>
 * 
 * @author Tal Liron
 * @see Package
 */
public class PackagesPlugin implements Plugin1
{
	//
	// Plugin
	//

	public int getInterfaceVersion()
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
			boolean verify = command.getSwitches().contains( "verify" );

			command.remove();
			command.getSincerity().getContainer().getDependencies().getPackages().install( filter, overwrite, verify );
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
	}
}

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

package com.threecrickets.sincerity.plugin;

import java.util.Set;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.packaging.ArtifactManager;
import com.threecrickets.sincerity.packaging.Packages;

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
		Sincerity sincerity = command.getSincerity();

		if( "unpack".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			String filter;
			if( arguments.length < 1 )
				filter = null;
			else
				filter = arguments[0];

			command.setParse( true );
			Set<String> switches = command.getSwitches();
			boolean overwrite = switches.contains( "overwrite" );
			boolean verify = switches.contains( "verify" );

			command.remove();

			Container<?, ?> container = sincerity.getContainer();
			Dependencies<?> dependencies = container.getDependencies();
			Packages packages = dependencies.getPackages();
			ArtifactManager managedArtifacts = dependencies.getArtifactManager();
			packages.install( managedArtifacts, filter, overwrite, verify );
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
	}
}

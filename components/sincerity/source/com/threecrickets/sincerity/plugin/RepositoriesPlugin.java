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

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Iterator;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.dependencies.Repositories;
import com.threecrickets.sincerity.dependencies.Repository;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.swing.RepositoriesPane;
import com.threecrickets.sincerity.util.TreeUtil;

/**
 * The repositories plugin supports the following commands:
 * <ul>
 * <li><b>repositories</b>: prints out a list of all repositories attached to
 * this container, organized by section ("public" and "private").</li>
 * <li><b>attach</b>: attaches a repository to this container. Supports one or
 * three or more arguments. If it's one argument, it is considered a reference
 * to an "attach"-type shortcut. If it's three ore more arguments, the first one
 * is the section ("public" or "private"), the second is a unique identifier in
 * this container for the repository, the third is the repository type
 * ("maven"/"ibiblio", "pypi/"python", etc.) and additional arguments are per
 * repository type.</li>
 * <li><b>detach</b>: detaches a repository from this container. Two arguments
 * are required: the section name and the unique identifier.</li>
 * </ul>
 * Additionally, this plugin adds a "Repositories" tab to the GUI.
 * 
 * @author Tal Liron
 * @see Repositories
 * @see RepositoriesPane
 */
public class RepositoriesPlugin implements Plugin1
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
		return "repositories";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"repositories", "attach", "detach"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		Sincerity sincerity = command.getSincerity();
		PrintWriter out = sincerity.getOut();
		PrintWriter err = sincerity.getErr();

		if( "repositories".equals( commandName ) )
		{
			Container<?, ?> container = sincerity.getContainer();
			Repositories repositories = container.getRepositories();

			printRepositories( out, "Private", repositories.get( "private" ) );
			printRepositories( out, "Public", repositories.get( "public" ) );
		}
		else if( "attach".equals( commandName ) )
		{
			command.setParse( true );
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "section (or shortcut)", "name", "type" );

			if( arguments.length == 1 )
			{
				String shortcut = arguments[0];
				command.remove();
				sincerity.run( Shortcuts.SHORTCUT_PREFIX + "attach" + Shortcuts.SHORTCUT_TYPE_SEPARATOR + shortcut );
				return;
			}

			String section = arguments[0];
			String name = arguments[1];
			String type = arguments[2];

			Container<?, ?> container = sincerity.getContainer();
			Repositories repositories = container.getRepositories();

			if( "maven".equals( type ) || "ibiblio".equals( type ) )
			{
				if( arguments.length < 4 )
					throw new BadArgumentsCommandException( command, "section", "name", "type", "url" );

				String url = arguments[3];

				if( !repositories.addMaven( section, name, url ) )
					if( sincerity.getVerbosity() >= 2 )
						err.println( "Repository already in use: " + section + ":" + name );
			}
			else if( "pypi".equals( type ) || "python".equals( type ) )
			{
				if( arguments.length < 4 )
					throw new BadArgumentsCommandException( command, "section", "name", "type", "url" );

				String url = arguments[3];

				if( !repositories.addPyPi( section, name, url ) )
					if( sincerity.getVerbosity() >= 2 )
						err.println( "Repository already in use: " + section + ":" + name );
			}
			else
				err.println( "Unknown repository type: " + type );
		}
		else if( "detach".equals( commandName ) )
		{
			command.setParse( true );
			String[] arguments = command.getArguments();
			if( arguments.length < 2 )
				throw new BadArgumentsCommandException( command, "section", "name" );

			String section = arguments[0];
			String name = arguments[1];

			Container<?, ?> container = sincerity.getContainer();
			Repositories repositories = container.getRepositories();

			if( !repositories.remove( section, name ) )
				if( sincerity.getVerbosity() >= 2 )
					err.println( "Repository was not in use: " + section + ":" + name );
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		sincerity.getFrame().getTabs().add( "Repositories", new RepositoriesPane( sincerity ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static void printRepositories( PrintWriter writer, String section, Collection<Repository> repositories )
	{
		if( repositories.isEmpty() )
			return;

		writer.println( section );
		for( Iterator<Repository> i = repositories.iterator(); i.hasNext(); )
		{
			Repository repository = i.next();
			writer.print( i.hasNext() ? TreeUtil.TVV : TreeUtil.LVV );
			printRepository( writer, repository );
			writer.println();
		}
	}

	private static void printRepository( PrintWriter writer, Repository repository )
	{
		String name = repository.getName();
		String[] split = name.split( Repositories.REPOSITORY_SECTION_SEPARATOR, 2 );
		if( split.length == 2 )
			writer.print( split[1] );
		else
			writer.print( name );

		writer.print( ": " );
		writer.print( repository.getType() );
		writer.print( ":" );
		writer.print( repository.getLocation() );
	}
}

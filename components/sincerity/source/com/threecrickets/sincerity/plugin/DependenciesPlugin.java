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

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ivy.core.module.descriptor.License;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.ResolvedDependencies;
import com.threecrickets.sincerity.ResolvedDependency;
import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.TreeUtil;
import com.threecrickets.sincerity.plugin.gui.AddDependenciesButton;
import com.threecrickets.sincerity.plugin.gui.DependenciesPane;
import com.threecrickets.sincerity.plugin.gui.Frame;
import com.threecrickets.sincerity.plugin.gui.LicensesPane;

/**
 * The dependencies plugin supports the following commands:
 * <ul>
 * <li><b>dependencies</b>: prints out the <i>resolved</i> dependency tree for
 * this container.</li>
 * <li><b>licenses</b>: prints out the licenses of all <i>resolved</i>
 * dependencies in this container. Use the --verbose switch for a more detailed
 * report.</li>
 * <li><b>reset</b>: removes all dependencies from this container. Note that
 * this does not actually uninstall them.</li>
 * <li><b>add</b>: adds a dependency to this container. Supports one, two or
 * three arguments. If it's one argument, it is considered a reference to an
 * "add"-type shortcut. If it's two arguments, they are the group and module
 * name of the dependency, leaving Sincerity to pick the highest available
 * version. If it's three arguments, they are the group, module name and version
 * of the dependency. Note that this does not actually install the dependency.</li>
 * <li><b>revise</b>: allows you to change the version of a previously added
 * dependency. The first two arguments are the group and module name, and the
 * third is the new version. Note that this does not actually install the
 * dependency.</li>
 * <li><b>remove</b>: removes a single dependency. The two arguments are group
 * and module name. Note that this does not actually uninstall the dependency.</li>
 * </ul>
 * Additionally, this plugin adds "Dependencies" and "Licenses" tabs and an
 * "Add and Install" button to the GUI.
 * 
 * @author Tal Liron
 * @see Dependencies
 * @see ResolvedDependencies
 * @see DependenciesPane
 * @see LicensesPane
 * @see AddDependenciesButton
 */
public class DependenciesPlugin implements Plugin1
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
		return "dependencies";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"dependencies", "licenses", "reset", "add", "revise", "remove", "exclude"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "dependencies".equals( commandName ) )
		{
			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			printTree( dependencies, command.getSincerity().getOut() );
		}
		else if( "licenses".equals( commandName ) )
		{
			command.setParse( true );
			boolean verbose = command.getSwitches().contains( "verbose" );

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			printLicenses( dependencies, command.getSincerity().getOut(), verbose );
		}
		else if( "reset".equals( commandName ) )
		{
			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			dependencies.reset();

			command.remove();
			command.getSincerity().reboot();
		}
		else if( "add".equals( commandName ) )
		{
			command.setParse( true );
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "group (or shortcut)", "name", "[version]" );

			if( arguments.length == 1 )
			{
				String shortcut = arguments[0];
				command.remove();
				command.getSincerity().run( Shortcuts.SHORTCUT_PREFIX + "add" + Shortcuts.SHORTCUT_TYPE_SEPARATOR + shortcut );
				return;
			}

			boolean force = command.getSwitches().contains( "force" );
			boolean only = command.getSwitches().contains( "only" );

			String group = arguments[0];
			String name = arguments[1];
			String version;
			if( arguments.length < 3 )
				version = "latest.integration";
			else
				version = arguments[2];

			if( "latest".equals( version ) )
				version = "latest.integration";

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			if( !dependencies.add( group, name, version, force, !only ) )
				if( command.getSincerity().getVerbosity() >= 2 )
					command.getSincerity().getErr().println( "Dependency already in container: " + group + ":" + name + ":" + version );
		}
		else if( "revise".equals( commandName ) )
		{
			command.setParse( true );
			String[] arguments = command.getArguments();
			if( arguments.length < 3 )
				throw new BadArgumentsCommandException( command, "group", "name", "version" );

			String group = arguments[0];
			String name = arguments[1];
			String version = arguments[2];

			if( "latest".equals( version ) )
				version = "latest.integration";

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			if( !dependencies.revise( group, name, version ) )
				if( command.getSincerity().getVerbosity() >= 2 )
					command.getSincerity().getErr().println( "Dependency not revised: " + group + ":" + name + ":" + version );
		}
		else if( "remove".equals( commandName ) )
		{
			command.setParse( true );
			String[] arguments = command.getArguments();
			if( arguments.length < 2 )
				throw new BadArgumentsCommandException( command, "group", "name" );

			String group = arguments[0];
			String name = arguments[1];

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			if( !dependencies.remove( group, name ) )
				if( command.getSincerity().getVerbosity() >= 2 )
					command.getSincerity().getErr().println( "Dependency was not in container: " + group + ":" + name );
		}
		else if( "exclude".equals( commandName ) )
		{
			command.setParse( true );
			String[] arguments = command.getArguments();
			if( arguments.length < 2 )
				throw new BadArgumentsCommandException( command, "group", "name" );

			String group = arguments[0];
			String name = arguments[1];

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			if( !dependencies.exclude( group, name ) )
				if( command.getSincerity().getVerbosity() >= 2 )
					command.getSincerity().getErr().println( "Exclusion already in container: " + group + ":" + name );
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		Frame frame = sincerity.getFrame();
		Dependencies dependencies = sincerity.getContainer().getDependencies();
		frame.getTabs().add( "Dependencies", new DependenciesPane( dependencies ) );
		frame.getTabs().add( "Licenses", new LicensesPane( dependencies ) );
		frame.getToolbar().add( new AddDependenciesButton( sincerity ) );
	}

	//
	// Operations
	//

	public void printTree( Dependencies dependencies, Writer writer ) throws SincerityException
	{
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		ArrayList<String> patterns = new ArrayList<String>();
		for( ResolvedDependency resolvedDependency : dependencies.getResolvedDependencies() )
			printTree( printWriter, resolvedDependency, patterns, false );
	}

	public void printLicenses( Dependencies depenencies, Writer writer, boolean verbose ) throws SincerityException
	{
		@SuppressWarnings("resource")
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		for( ResolvedDependency resolvedDependency : depenencies.getResolvedDependencies().getAll() )
		{
			License[] licenses = resolvedDependency.descriptor.getLicenses();
			int length = licenses.length;
			if( length == 0 )
				continue;
			printWriter.println( resolvedDependency );
			for( int i = 0; i < length; i++ )
			{
				License license = licenses[i];
				printWriter.print( i == length - 1 ? TreeUtil.LVV : TreeUtil.TVV );
				printWriter.print( license.getName() );
				if( verbose )
				{
					printWriter.print( ": " );
					printWriter.print( license.getUrl() );
				}
				printWriter.println();
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static void printTree( PrintWriter writer, ResolvedDependency resolvedDependency, ArrayList<String> patterns, boolean seal )
	{
		int size = patterns.size();

		if( size > 0 )
		{
			for( Iterator<String> i = patterns.iterator(); i.hasNext(); )
			{
				String pattern = i.next();
				if( !i.hasNext() )
				{
					// Last pattern depends on whether we are sealing
					if( seal )
						pattern = size < 2 ? TreeUtil.L : TreeUtil._L;
					else
						pattern = size < 2 ? TreeUtil.T : TreeUtil._T;
				}
				writer.print( pattern );
			}

			writer.print( TreeUtil.VV );
			if( seal )
				// Erase the pattern after it was sealed
				patterns.set( size - 1, size < 2 ? "  " : "    " );
		}

		writer.println( resolvedDependency );

		if( !resolvedDependency.children.isEmpty() )
		{
			patterns.add( size == 0 ? TreeUtil.I : TreeUtil._I );

			for( Iterator<ResolvedDependency> i = resolvedDependency.children.iterator(); i.hasNext(); )
			{
				ResolvedDependency child = i.next();
				printTree( writer, child, patterns, !i.hasNext() );
			}

			patterns.remove( patterns.size() - 1 );
		}
	}
}

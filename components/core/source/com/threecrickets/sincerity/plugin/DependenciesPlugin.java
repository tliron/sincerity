package com.threecrickets.sincerity.plugin;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.ivy.core.module.descriptor.License;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.ResolvedDependency;
import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.TreeUtil;

public class DependenciesPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "dependencies";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"dependencies", "licenses", "install", "uninstall", "reset", "add", "revise", "remove"
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
		else if( "install".equals( commandName ) )
		{
			command.setParse( true );

			String[] arguments = command.getArguments();
			String name;
			if( arguments.length < 1 )
				name = null;
			else
				name = arguments[0];

			if( name == null )
			{
				boolean overwrite = command.getSwitches().contains( "overwrite" );
				Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
				dependencies.install( overwrite );
			}
			else
				command.getSincerity().run( Shortcuts.SHORTCUT_PREFIX + "install." + name );
		}
		else if( "uninstall".equals( commandName ) )
		{
			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			dependencies.uninstall();
		}
		else if( "reset".equals( commandName ) )
		{
			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			dependencies.reset();
		}
		else if( "add".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 2 )
				throw new BadArgumentsCommandException( command, "group", "name", "[version]" );

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
			if( !dependencies.add( group, name, version ) )
				command.getSincerity().getErr().println( "Dependency already in container: " + group + ":" + name + ":" + version );
		}
		else if( "revise".equals( commandName ) )
		{
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
				command.getSincerity().getErr().println( "Dependency not revised: " + group + ":" + name + ":" + version );

		}
		else if( "remove".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 2 )
				throw new BadArgumentsCommandException( command, "group", "name" );

			String group = arguments[0];
			String name = arguments[1];

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			if( !dependencies.remove( group, name ) )
				command.getSincerity().getErr().println( "Dependency was not in container: " + group + ":" + name );
		}
		else
			throw new UnknownCommandException( command );
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
				System.out.print( pattern );
			}

			System.out.print( TreeUtil.VV );
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

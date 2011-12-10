package com.threecrickets.sincerity.plugin;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.ivy.core.module.descriptor.License;
import org.xml.sax.SAXException;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Package;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.ResolvedDependency;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

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
			"dependencies", "licenses", "install", "unpack", "reset", "add", "remove"
		};
	}

	public void run( Command command ) throws Exception
	{
		String name = command.getName();
		if( "dependencies".equals( name ) )
		{
			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			printTree( dependencies, new OutputStreamWriter( System.out ) );
		}
		else if( "licenses".equals( name ) )
		{
			boolean verbose = command.getSwitches().contains( "verbose" );

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			printLicenses( dependencies, new OutputStreamWriter( System.out ), verbose );
		}
		else if( "install".equals( name ) )
		{
			boolean overwrite = "true".equals( command.getProperties().get( "overwrite" ) );

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			dependencies.install( overwrite );
		}
		else if( "unpack".equals( name ) )
		{
			String[] arguments = command.getArguments();
			String packageName;
			if( arguments.length < 1 )
				packageName = null;
			else
				packageName = arguments[0];

			boolean overwrite = command.getSwitches().contains( "overwrite" );

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			if( packageName == null )
				dependencies.getPackages().unpack( overwrite );
			else
			{
				Package pack = dependencies.getPackages().get( packageName );
				if( pack == null )
					throw new Exception( "Unknown package: " + packageName );
				pack.unpack( overwrite );
			}
		}
		else if( "reset".equals( name ) )
		{
			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			dependencies.reset();
		}
		else if( "add".equals( name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 2 )
				throw new BadArgumentsCommandException( command, "group", "name", "[version]" );

			String organisation = arguments[0];
			String dependencyName = arguments[1];
			String revision;
			if( arguments.length < 3 )
				revision = "latest.integration";
			else
				revision = arguments[2];

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			if( !dependencies.add( organisation, dependencyName, revision ) )
				System.err.println( "Dependency already in container: " + organisation + ":" + dependencyName + ":" + revision );
		}
		else if( "remove".equals( name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 3 )
				throw new BadArgumentsCommandException( command, "group", "name", "version" );

			String organisation = arguments[0];
			String dependencyName = arguments[1];
			String revision = arguments[2];

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			if( !dependencies.remove( organisation, dependencyName, revision ) )
				System.err.println( "Dependency was not in container: " + organisation + ":" + dependencyName + ":" + revision );
		}
		else
			throw new UnknownCommandException( command );
	}

	//
	// Operations
	//

	public void printTree( Dependencies dependencies, Writer writer ) throws ParserConfigurationException, SAXException, IOException
	{
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		ArrayList<String> patterns = new ArrayList<String>();
		for( ResolvedDependency resolvedDependency : dependencies.getResolvedDependencies() )
			printTree( printWriter, resolvedDependency, patterns, false );
	}

	public void printLicenses( Dependencies depenencies, Writer writer, boolean verbose ) throws ParserConfigurationException, SAXException, IOException
	{
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		for( ResolvedDependency resolvedDependency : depenencies.getResolvedDependencies().getInstalledDependencies() )
		{
			License[] licenses = resolvedDependency.descriptor.getLicenses();
			int length = licenses.length;
			if( length == 0 )
				continue;
			printWriter.println( resolvedDependency );
			for( int i = 0; i < length; i++ )
			{
				License license = licenses[i];
				printWriter.print( i == length - 1 ? " \u2514\u2500\u2500" : " \u251C\u2500\u2500" );
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
						pattern = size < 2 ? " \u2514" : "   \u2514";
					else
						pattern = size < 2 ? " \u251C" : "   \u251C";
				}
				System.out.print( pattern );
			}

			System.out.print( "\u2500\u2500" );
			if( seal )
				// Erase the pattern after it was sealed
				patterns.set( size - 1, size < 2 ? "  " : "    " );
		}

		writer.println( resolvedDependency );

		if( !resolvedDependency.children.isEmpty() )
		{
			patterns.add( size == 0 ? " \u2502" : "   \u2502" );

			for( Iterator<ResolvedDependency> i = resolvedDependency.children.iterator(); i.hasNext(); )
			{
				ResolvedDependency child = i.next();
				printTree( writer, child, patterns, !i.hasNext() );
			}

			patterns.remove( patterns.size() - 1 );
		}
	}
}

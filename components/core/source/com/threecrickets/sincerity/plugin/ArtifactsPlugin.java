package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.xml.sax.SAXException;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.ResolvedDependency;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class ArtifactsPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "artifacts";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"artifacts", "resolve", "clean", "prune"
		};
	}

	public void run( Command command, Sincerity sincerity ) throws Exception
	{
		Dependencies dependencies = sincerity.getContainer().getDependencies();

		if( "artifacts".equals( command.name ) )
		{
			boolean verbose = command.getSwitches().contains( "verbose" );

			printArtifacts( dependencies, new OutputStreamWriter( System.out ), verbose );
		}
		else if( "clean".equals( command.name ) )
		{
			dependencies.clean();
		}
		else if( "prune".equals( command.name ) )
		{
			dependencies.prune();
		}
		else
			throw new UnknownCommandException( command );
	}

	//
	// Operations
	//

	public void printArtifacts( Dependencies dependencies, Writer writer, boolean verbose ) throws IOException, ParserConfigurationException, SAXException
	{
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		for( ResolvedDependency resolvedDependency : dependencies.getResolvedDependencies().getInstalledDependencies() )
		{
			printWriter.println( resolvedDependency );
			org.apache.ivy.core.module.descriptor.Artifact[] artifacts = resolvedDependency.descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION );
			for( int length = artifacts.length, i = 0; i < length; i++ )
			{
				org.apache.ivy.core.module.descriptor.Artifact artifact = artifacts[i];
				printWriter.print( i == length - 1 ? " \u2514\u2500\u2500" : " \u251C\u2500\u2500" );

				String location = artifact.getId().getAttribute( "location" );
				boolean installed = location != null && new File( location ).exists();

				if( !installed )
					printWriter.print( "(" );

				printWriter.print( artifact.getType() );

				if( verbose )
				{
					printWriter.print( ": " );
					String size = artifact.getId().getAttribute( "size" );
					if( location != null )
						printWriter.print( dependencies.getContainer().getRelativePath( location ) );
					else
					{
						// Could not find a location for it?
						printWriter.print( artifact.getName() );
						printWriter.print( '.' );
						printWriter.print( artifact.getExt() );
						printWriter.print( '?' );
					}
					if( size != null )
					{
						printWriter.print( " (" );
						printWriter.print( size );
						printWriter.print( " bytes)" );
					}
				}

				if( !installed )
					printWriter.print( ")" );

				printWriter.println();
			}
		}
	}
}

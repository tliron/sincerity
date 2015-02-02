/**
 * Copyright 2011-2014 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;

import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;

import com.threecrickets.sincerity.Artifact;
import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Package;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.ResolvedDependency;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.gui.ArtifactsPane;
import com.threecrickets.sincerity.util.TreeUtil;

/**
 * The artifact plugin supports the following commands:
 * <ul>
 * <li><b>artifacts</b>: prints out a list of artifacts installed in the current
 * container, organized by jars. Use the --verbose switch for a more detailed
 * report.</li>
 * <li><b>install</b>: downloads and installs all artifacts for dependencies in
 * this container. This would also involve unpacking all packages and running
 * their installation hooks.</li>
 * <li><b>uninstall</b>: uninstalls all artifacts in this container. This would
 * also involve calling all package uninstall hooks. Note that the dependencies
 * are still added to the container, and can be re-installed. Also see
 * "container:clean".</li>
 * <li><b>prune</b> : deletes any artifacts that have been previously installed
 * but are no longer necessary due to changed in the dependencies. Note that
 * changed artifacts will be ignored.</li>
 * </ul>
 * Additionally, this plugin adds an "Artifacts" tab to the GUI.
 * 
 * @author Tal Liron
 * @see Artifact
 * @see ArtifactsPane
 */
public class ArtifactsPlugin implements Plugin1
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
		return "artifacts";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"artifacts", "install", "uninstall", "prune"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "artifacts".equals( commandName ) )
		{
			command.setParse( true );
			boolean packages = command.getSwitches().contains( "packages" );
			boolean verbose = command.getSwitches().contains( "verbose" );

			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			printArtifacts( dependencies, command.getSincerity().getOut(), packages, verbose );
		}
		else if( "install".equals( commandName ) )
		{
			command.setParse( true );
			boolean overwrite = command.getSwitches().contains( "overwrite" );
			boolean verify = command.getSwitches().contains( "verify" );

			Container container = command.getSincerity().getContainer();
			container.getDependencies().install( overwrite, verify );

			if( container.hasFinishedInstalling() )
			{
				container.setInstallations( 0 );
				command.remove();
			}
			command.getSincerity().reboot();
		}
		else if( "uninstall".equals( commandName ) )
		{
			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			dependencies.uninstall();

			command.remove();
			command.getSincerity().reboot();
		}
		else if( "prune".equals( commandName ) )
		{
			Dependencies dependencies = command.getSincerity().getContainer().getDependencies();
			dependencies.prune();

			command.remove();
			command.getSincerity().reboot();
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		sincerity.getFrame().getTabs().add( "Artifacts", new ArtifactsPane( sincerity.getContainer().getDependencies() ) );
	}

	//
	// Operations
	//

	public void printArtifacts( Dependencies dependencies, Writer writer, boolean packages, boolean verbose ) throws SincerityException
	{
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		for( ResolvedDependency resolvedDependency : dependencies.getResolvedDependencies().getAll() )
		{
			printWriter.println( resolvedDependency );
			org.apache.ivy.core.module.descriptor.Artifact[] artifacts = resolvedDependency.descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION );
			for( int length = artifacts.length, i = 0; i < length; i++ )
			{
				org.apache.ivy.core.module.descriptor.Artifact artifact = artifacts[i];
				printWriter.print( i == length - 1 ? TreeUtil.LVV : TreeUtil.TVV );

				String location = artifact.getId().getAttribute( "location" );
				if( location != null )
					location = dependencies.getContainer().getRelativePath( location );
				boolean installed = location != null && new File( location ).exists();

				if( !installed )
					printWriter.print( '(' );

				printWriter.print( artifact.getType() );

				if( verbose )
				{
					printWriter.print( ": " );
					String size = artifact.getId().getAttribute( "size" );
					if( location != null )
						printWriter.print( location );
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
					printWriter.print( ')' );

				printWriter.println();

				if( packages )
				{
					Package pack = location != null ? dependencies.getPackages().getPackage( new File( location ) ) : null;
					if( pack != null )
					{
						for( Iterator<Artifact> ii = pack.iterator(); ii.hasNext(); )
						{
							Artifact packedArtifact = ii.next();
							printWriter.print( "    " );
							printWriter.print( ii.hasNext() ? TreeUtil.TVV : TreeUtil.LVV );
							printWriter.println( dependencies.getContainer().getRelativeFile( packedArtifact.getFile() ) );
						}
					}
				}
			}
		}
	}
}

/**
 * Copyright 2011-2016 Three Crickets LLC.
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
import java.util.Set;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.dependencies.Artifact;
import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.dependencies.Module;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.packaging.Package;
import com.threecrickets.sincerity.plugin.swing.ArtifactsPane;
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
		Sincerity sincerity = command.getSincerity();
		PrintWriter out = sincerity.getOut();

		if( "artifacts".equals( commandName ) )
		{
			command.setParse( true );
			Set<String> switches = command.getSwitches();
			boolean packages = switches.contains( "packages" );
			boolean verbose = switches.contains( "verbose" );

			Container<?, ?> container = sincerity.getContainer();
			Dependencies<?> dependencies = container.getDependencies();

			printArtifacts( dependencies, out, packages, verbose );
		}
		else if( "install".equals( commandName ) )
		{
			command.setParse( true );
			Set<String> switches = command.getSwitches();
			boolean overwrite = switches.contains( "overwrite" );
			boolean verify = switches.contains( "verify" );

			Container<?, ?> container = sincerity.getContainer();
			Dependencies<?> dependencies = container.getDependencies();

			dependencies.install( overwrite, verify );

			if( container.hasFinishedInstalling() )
			{
				container.setInstallations( 0 );
				command.remove();
			}
			command.getSincerity().reboot();
		}
		else if( "uninstall".equals( commandName ) )
		{
			Container<?, ?> container = sincerity.getContainer();
			Dependencies<?> dependencies = container.getDependencies();

			dependencies.uninstallPackages();

			command.remove();
			command.getSincerity().reboot();
		}
		else if( "prune".equals( commandName ) )
		{
			Container<?, ?> container = sincerity.getContainer();
			Dependencies<?> dependencies = container.getDependencies();

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

	public void printArtifacts( Dependencies<?> dependencies, Writer writer, boolean withPackages, boolean verbose ) throws SincerityException
	{
		Container<?, ?> container = dependencies.getContainer();
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		for( Module module : dependencies.getModules().getAll() )
		{
			printWriter.println( module );
			for( Iterator<Artifact> i = module.getArtifacts().iterator(); i.hasNext(); )
			{
				Artifact artifact = i.next();
				printWriter.print( i.hasNext() ? TreeUtil.TVV : TreeUtil.LVV );

				File location = artifact.getLocation();
				if( location != null )
					location = container.getRelativeFile( location );
				boolean installed = location != null && location.exists();

				if( !installed )
					printWriter.print( '(' );

				printWriter.print( artifact.getType() );

				if( verbose )
				{
					printWriter.print( ": " );
					Integer size = artifact.getSize();
					if( location != null )
						printWriter.print( location );
					else
					{
						// Could not find a location for it?
						printWriter.print( artifact.getName() );
						printWriter.print( '.' );
						printWriter.print( artifact.getExtension() );
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

				if( withPackages )
				{
					Package pack = location != null ? dependencies.getPackages().getPackage( location ) : null;
					if( pack != null )
					{
						for( Iterator<com.threecrickets.sincerity.packaging.Artifact> ii = pack.iterator(); ii.hasNext(); )
						{
							com.threecrickets.sincerity.packaging.Artifact packedArtifact = ii.next();
							printWriter.print( "    " );
							printWriter.print( ii.hasNext() ? TreeUtil.TVV : TreeUtil.LVV );
							printWriter.println( container.getRelativeFile( packedArtifact.getFile() ) );
						}
					}
				}
			}
		}
	}
}

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

import java.io.File;
import java.io.IOException;
import java.util.Set;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.swing.CreateContainerButton;
import com.threecrickets.sincerity.util.IoUtil;

/**
 * The container plugin supports the following commands:
 * <ul>
 * <li><b>create</b>: creates a new container. The first argument is required,
 * and is the container root path. The second optional argument is the template
 * to use. It defaults to "default". If the directory already exists, the
 * command will fail, unless the --force switch is used, in which case the
 * template will be force-copied into the directory (which may result in files
 * being overriden). Note that this command will cause Sincerity to reboot if
 * successful.</li>
 * <li><b>use</b>: switches to an existing container. The required argument is
 * the container root path. Note that this command will cause Sincerity to
 * reboot if successful.</li>
 * <li><b>clone</b>: similar to "create", except that the current container will
 * be used as the template for the new container. The --force switch is also
 * supported. Note that this command will cause Sincerity to reboot if
 * successful.</li>
 * <li><b>clean</b>: uninstalls all dependencies and deletes this container's
 * "/cache/" directory. Also see "dependencies:uninstall".</li>
 * </ul>
 * Additionally, this plugin adds a "Create" button to the GUI.
 * 
 * @author Tal Liron
 * @see Container
 * @see CreateContainerButton
 */
public class ContainerPlugin implements Plugin1
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
		return "container";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"create", "use", "clone", "clean"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		Sincerity sincerity = command.getSincerity();

		if( "create".equals( commandName ) )
		{
			command.setParse( true );
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path", "[template]" );
			Set<String> switches = command.getSwitches();

			File containerRoot = new File( arguments[0] );
			String template;
			if( arguments.length < 2 )
				template = "default";
			else
				template = arguments[1];
			boolean force = switches.contains( "force" );
			File templateDir = new File( new File( sincerity.getHome(), "templates" ), template );

			// TODO: look for templates according to ~/.sincerity/sincerity.conf
			// first (likely ~/.sincerity/templates
			// same for 'templatize'

			command.remove();
			sincerity.createContainer( containerRoot, templateDir, force );
		}
		else if( "use".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "container root path" );

			File containerRoot = new File( arguments[0] );
			if( !containerRoot.isDirectory() )
				throw new NoContainerException( "The container root path is not a folder: " + containerRoot );

			if( !new File( containerRoot, Container.SINCERITY_DIR ).isDirectory() )
				throw new NoContainerException( "The folder is not a valid container: " + containerRoot );

			command.remove();
			sincerity.setContainerRoot( containerRoot );
		}
		else if( "clone".equals( commandName ) )
		{
			command.setParse( true );
			String[] arguments = command.getArguments();
			Set<String> switches = command.getSwitches();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "target container root path" );
			boolean force = switches.contains( "force" );

			File containerRoot = new File( arguments[0] );

			Container<?, ?> container = sincerity.getContainer();

			command.remove();
			sincerity.createContainer( containerRoot, container.getRoot(), force );
		}
		else if( "clean".equals( commandName ) )
		{
			Container<?, ?> container = sincerity.getContainer();
			Dependencies<?> dependencies = container.getDependencies();

			dependencies.uninstallPackages();

			File cache = container.getFile( "cache" );
			if( cache.isDirectory() )
			{
				try
				{
					IoUtil.deleteRecursive( cache );
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not clean cache", x );
				}
			}

			command.remove();
			sincerity.reboot();
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
		Sincerity sincerity = command.getSincerity();
		sincerity.getFrame().getToolbar().add( new CreateContainerButton( sincerity ) );
	}
}

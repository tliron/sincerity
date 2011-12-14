package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.threecrickets.sincerity.exception.AmbiguousCommandException;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class Sincerity implements Runnable
{
	//
	// Constants
	//

	public static final String CONTAINER_PROPERTY = "sincerity.container";

	public static final String CONTAINER_ENV = "SINCERITY_CONTAINER";

	public static final String DEBUG_PROPERTY = "sincerity.debug";

	public static final String DEBUG_ENV = "SINCERITY_DEBUG";

	//
	// Static attributes
	//

	public static Sincerity getCurrent()
	{
		return threadLocal.get();
	}

	public static void setCurrent( Sincerity sincerity )
	{
		threadLocal.set( sincerity );
	}

	//
	// Main
	//

	public static void main( String[] arguments )
	{
		try
		{
			Sincerity sincerity = new Sincerity( arguments );
			setCurrent( sincerity );
			sincerity.run();
		}
		catch( SincerityException x )
		{
			System.err.println( x.getMessage() );
			System.exit( 1 );
		}
		catch( Throwable x )
		{
			x.printStackTrace();
			System.exit( 1 );
		}
	}

	//
	// Construction
	//

	public Sincerity( String[] arguments ) throws SincerityException
	{
		commands = parseCommands( arguments );
	}

	//
	// Attributes
	//

	public File getHome() throws SincerityException
	{
		if( sincerityHome == null )
		{
			String home = System.getProperty( "sincerity.home" );
			if( home == null )
				home = System.getenv( "SINCERITY_HOME" );
			if( home == null )
				home = ".";
			try
			{
				sincerityHome = new File( home ).getCanonicalFile();
			}
			catch( IOException x )
			{
				throw new SincerityException( "Could not determine location of Sincerity home: " + home, x );
			}
		}
		return sincerityHome;
	}

	public Plugins getPlugins() throws SincerityException
	{
		if( plugins != null )
		{
			if( plugins.getClassLoader() != getContainer().getDependencies().getClassLoader() )
				plugins = null;
		}

		if( plugins == null )
			plugins = new Plugins( this );

		return plugins;
	}

	public Container getContainer() throws SincerityException
	{
		if( container == null )
		{
			//
			// Look for container in this order:
			//
			// 1. the last 'container' command
			// 2. 'sincerity.container' JVM property
			// 3. 'SINCERITY_CONTAINER' environment variable
			// 4. Search up filesystem tree from current path
			//

			if( containerLocation == null )
			{
				containerLocation = System.getProperty( CONTAINER_PROPERTY );
				if( containerLocation == null )
					containerLocation = System.getenv( CONTAINER_ENV );
			}

			File containerRootDir = null;
			if( containerLocation != null )
			{
				try
				{
					containerRootDir = new File( containerLocation ).getCanonicalFile();
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not determine location of container: " + containerLocation, x );
				}

				if( !containerRootDir.exists() )
					throw new SincerityException( "Specified root path for the Sincerity container does not point anywhere: " + containerRootDir );
				if( !containerRootDir.isDirectory() )
					throw new SincerityException( "Specified root path for the Sincerity container does not point to a directory: " + containerRootDir );
				File sincerityDir = new File( containerRootDir, Container.SINCERITY_DIR );
				if( !sincerityDir.isDirectory() )
					throw new SincerityException( "Specified root path for the Sincerity container does not point to a valid container: " + containerRootDir );
			}
			else
			{
				File currentDir;
				try
				{
					currentDir = new File( "." ).getCanonicalFile();
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not determine location of current directory", x );
				}
				containerRootDir = currentDir;
				while( true )
				{
					File sincerityDir = new File( containerRootDir, Container.SINCERITY_DIR );
					if( sincerityDir.isDirectory() )
					{
						// Found it!
						break;
					}
					containerRootDir = containerRootDir.getParentFile();
					if( containerRootDir == null )
						throw new NoContainerException( "Could not find a Sincerity container for the current directory: " + currentDir );
					try
					{
						containerRootDir = containerRootDir.getCanonicalFile();
					}
					catch( IOException x )
					{
						throw new SincerityException( "Could not determine location of container: " + containerRootDir, x );
					}
				}
			}

			String debug = System.getProperty( DEBUG_PROPERTY );
			if( debug == null )
				debug = System.getenv( DEBUG_ENV );
			int debugLevel = 1;
			if( debug != null )
			{
				try
				{
					debugLevel = Integer.parseInt( debug );
				}
				catch( Exception x )
				{
					throw new SincerityException( "Sincerity debug value must be a number" );
				}
			}

			container = new Container( containerRootDir, debugLevel );

			System.out.println( "Using Sincerity container at: " + containerRootDir );
		}

		return container;
	}

	public void setContainer( String containerLocation )
	{
		this.containerLocation = containerLocation;
		container = null;
	}

	//
	// Runnable
	//

	public void run()
	{
		if( commands.isEmpty() )
			commands.add( new Command( "help" + Command.PLUGIN_COMMAND_SEPARATOR + "help", this ) );

		try
		{
			for( Command command : commands )
				run( command );
		}
		catch( SincerityException x )
		{
			System.err.println( x.getMessage() );
			// x.printStackTrace();
			System.exit( 1 );
		}
		catch( Throwable x )
		{
			x.printStackTrace();
			System.exit( 1 );
		}
	}

	//
	// Operations
	//

	public List<Command> parseCommands( String[] arguments )
	{
		ArrayList<Command> commands = new ArrayList<Command>();

		Command command = null;
		boolean isGreedy = false;

		for( String argument : arguments )
		{
			if( argument.length() == 0 )
				continue;

			if( !isGreedy && Command.COMMANDS_SEPARATOR.equals( argument ) )
			{
				if( command != null )
				{
					commands.add( command );
					command = null;
				}
			}
			else
			{
				if( command == null )
				{
					if( argument.endsWith( Command.GREEDY_POSTFIX ) )
					{
						isGreedy = true;
						argument = argument.substring( 0, argument.length() - Command.GREEDY_POSTFIX_LENGTH );
					}
					command = new Command( argument, this );
				}
				else
					command.rawArguments.add( argument );
			}
		}

		if( command != null )
			commands.add( command );

		return commands;
	}

	public void run( Command command ) throws SincerityException
	{
		if( command.getName().startsWith( Shortcuts.SHORTCUT_PREFIX ) )
		{
			String[] shortcut = getContainer().getShortcuts().get( command.getName().substring( Shortcuts.SHORTCUT_PREFIX_LENGTH ) );
			if( shortcut != null )
			{
				List<Command> commands = parseCommands( shortcut );
				for( Command c : commands )
					run( c );
			}
			return;
		}

		if( command.plugin != null )
		{
			Plugin thePlugin = getPlugins().get( command.plugin );
			if( thePlugin == null )
				throw new UnknownCommandException( command );
			thePlugin.run( command );
		}
		else
		{
			ArrayList<Plugin> plugins = new ArrayList<Plugin>();
			for( Plugin plugin : getPlugins().values() )
			{
				if( Arrays.asList( plugin.getCommands() ).contains( command.getName() ) )
					plugins.add( plugin );
			}

			int size = plugins.size();
			if( size == 1 )
			{
				Plugin plugin = plugins.get( 0 );
				command.plugin = plugin.getName();
				plugin.run( command );
				return;
			}
			else if( size > 1 )
				throw new AmbiguousCommandException( command, plugins );
			else
				throw new UnknownCommandException( command );
		}
	}

	public void run( String name, String... arguments ) throws SincerityException
	{
		if( name.endsWith( Command.GREEDY_POSTFIX ) )
			name = name.substring( 0, name.length() - Command.GREEDY_POSTFIX_LENGTH );

		Command command = new Command( name, this );
		for( String argument : arguments )
			command.rawArguments.add( argument );
		run( command );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<Command> commands;

	private File sincerityHome;

	private String containerLocation;

	private Container container;

	private Plugins plugins;

	private static final ThreadLocal<Sincerity> threadLocal = new ThreadLocal<Sincerity>();
}

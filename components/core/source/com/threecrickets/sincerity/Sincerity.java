package com.threecrickets.sincerity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.threecrickets.sincerity.exception.AmbiguousCommandException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class Sincerity implements Runnable
{
	//
	// Main
	//

	public static void main( String[] arguments )
	{
		try
		{
			Sincerity sincerity = new Sincerity( arguments );
			sincerity.run();
		}
		catch( Throwable x )
		{
			x.printStackTrace();
		}
	}

	//
	// Construction
	//

	public Sincerity( String[] arguments ) throws Exception
	{
		// Parse arguments
		Command command = null;
		boolean isGreedy = false;
		for( String argument : arguments )
		{
			if( argument.length() == 0 )
				continue;

			if( !isGreedy && ":".equals( argument ) )
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
					if( argument.endsWith( "!" ) )
					{
						isGreedy = true;
						argument = argument.substring( 0, argument.length() - 1 );
					}
					command = new Command( argument, this, !isGreedy );
				}
				else
					command.rawArguments.add( argument );
			}
		}
		if( command != null )
			commands.add( command );
	}

	//
	// Attributes
	//

	public Plugins getPlugins() throws Exception
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

	public Container getContainer() throws Exception
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
				containerLocation = System.getProperty( "sincerity.container" );
				if( containerLocation == null )
					containerLocation = System.getenv( "SINCERITY_CONTAINER" );
			}

			File containerRootDir = null;
			if( containerLocation != null )
			{
				containerRootDir = new File( containerLocation ).getCanonicalFile();
				if( !containerRootDir.exists() )
					throw new Exception( "Specified root path for the Sincerity container does not point anywhere: " + containerRootDir );
				if( !containerRootDir.isDirectory() )
					throw new Exception( "Specified root path for the Sincerity container does not point to a directory: " + containerRootDir );
				File sincerityDir = new File( containerRootDir, Container.SINCERITY_DIR_NAME );
				if( !sincerityDir.isDirectory() )
					throw new Exception( "Specified root path for the Sincerity container does not point to a valid container: " + containerRootDir );
			}
			else
			{
				File currentDir = new File( "." ).getCanonicalFile();
				containerRootDir = currentDir;
				while( true )
				{
					File sincerityDir = new File( containerRootDir, Container.SINCERITY_DIR_NAME );
					if( sincerityDir.isDirectory() )
					{
						// Found it!
						break;
					}
					containerRootDir = containerRootDir.getParentFile().getCanonicalFile();
					if( containerRootDir == null )
						throw new Exception( "Could not find a Sincerity container for the current directory: " + currentDir );
				}
			}

			String debug = System.getProperty( "sincerity.debug" );
			if( debug == null )
				debug = System.getenv( "SINCERITY_DEBUG" );
			int debugLevel = 1;
			if( debug != null )
			{
				try
				{
					debugLevel = Integer.parseInt( debug );
				}
				catch( Exception x )
				{
					throw new Exception( "Sincerity debug value must be a number" );
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
			commands.add( new Command( "help", this, false ) );

		try
		{
			for( Command command : commands )
				run( command );
		}
		catch( Exception x )
		{
			x.printStackTrace();
		}
	}

	//
	// Operations
	//

	public void run( Command command ) throws Exception
	{
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

	public void run( String name, String... arguments ) throws Exception
	{
		boolean isGreedy = false;
		if( name.endsWith( "!" ) )
		{
			isGreedy = true;
			name = name.substring( 0, name.length() - 1 );
		}

		Command command = new Command( name, this, !isGreedy );
		for( String argument : arguments )
			command.rawArguments.add( argument );
		run( command );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<Command> commands = new ArrayList<Command>();

	private String containerLocation;

	private Container container;

	private Plugins plugins;
}

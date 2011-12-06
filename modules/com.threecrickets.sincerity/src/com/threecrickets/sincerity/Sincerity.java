package com.threecrickets.sincerity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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
		ArrayList<String> statement = null;
		boolean inBootstrap = false;
		for( String argument : arguments )
		{
			if( argument.length() == 0 )
				continue;

			if( !inBootstrap && ":".equals( argument ) )
			{
				if( statement != null && !statement.isEmpty() )
				{
					statements.add( statement );
					statement = null;
				}
			}
			else if( !inBootstrap && argument.startsWith( "--" ) )
			{
				argument = argument.substring( 2 );
				if( argument.length() > 0 )
					switches.add( argument );
			}
			else
			{
				if( statement == null )
				{
					statement = new ArrayList<String>();
					if( "bootstrap".equals( argument ) )
						inBootstrap = true;
				}
				statement.add( argument );
			}
		}
		if( statement != null && !statement.isEmpty() )
			statements.add( statement );

		// Parse properties
		for( String theSwitch : switches )
		{
			String[] split = theSwitch.split( "=", 2 );
			if( split.length == 2 )
				properties.put( split[0], split[1] );
		}
	}

	//
	// Attributes
	//

	public Map<String, String> getProperties()
	{
		return properties;
	}

	public Plugins getPlugins() throws Exception
	{
		if( plugins == null )
			plugins = new Plugins( getContainer() );
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

			String debug = properties.get( "debug" );
			int debugLevel = 1;
			if( debug != null )
			{
				try
				{
					debugLevel = Integer.parseInt( debug );
				}
				catch( Exception x )
				{
					throw new Exception( "'--debug' value must be a number" );
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

	// Runnable
	//

	public void run()
	{
		if( statements.isEmpty() )
		{
			ArrayList<String> statement = new ArrayList<String>();
			statement.add( "help" );
			statements.add( statement );
		}

		try
		{
			for( ArrayList<String> statement : statements )
				run( statement.toArray( new String[statement.size()] ) );
		}
		catch( Exception x )
		{
			x.printStackTrace();
		}
	}

	//
	// Operations
	//

	public void run( String[] statement ) throws Exception
	{
		String command = statement[0];
		String[] arguments = new String[statement.length - 1];
		System.arraycopy( statement, 1, arguments, 0, arguments.length );
		run( command, arguments );
	}

	public void run( String command, String[] arguments ) throws Exception
	{
		String[] split = command.split( ":", 2 );
		if( split.length == 2 )
			run( split[0], split[1], arguments );
		else
		{
			ArrayList<Plugin> plugins = new ArrayList<Plugin>();
			for( Plugin plugin : getPlugins().values() )
			{
				if( Arrays.asList( plugin.getCommands() ).contains( command ) )
				{
					plugins.add( plugin );
				}
			}

			int size = plugins.size();
			if( size == 1 )
			{
				plugins.get( 0 ).run( command, arguments, this );
				return;
			}
			else if( size > 1 )
			{
				StringBuilder s = new StringBuilder( "Ambiguous command: " );
				for( Iterator<Plugin> i = plugins.iterator(); i.hasNext(); )
				{
					Plugin plugin = i.next();
					s.append( plugin.getName() );
					s.append( ':' );
					s.append( command );
					if( i.hasNext() )
						s.append( ", " );
				}
				throw new Exception( s.toString() );
			}
			else
				throw new Exception( "Unknown command: " + command );
		}
	}

	public void run( String plugin, String command, String[] arguments ) throws Exception
	{
		Plugin thePlugin = getPlugins().get( plugin );
		if( thePlugin == null )
			throw new Exception( "Unknown plugin: " + plugin );
		thePlugin.run( command, arguments, this );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final HashSet<String> switches = new HashSet<String>();

	private final HashMap<String, String> properties = new HashMap<String, String>();

	private final ArrayList<ArrayList<String>> statements = new ArrayList<ArrayList<String>>();

	private String containerLocation;

	private Container container;

	private Plugins plugins;
}

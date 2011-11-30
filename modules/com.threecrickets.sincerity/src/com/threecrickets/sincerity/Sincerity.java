package com.threecrickets.sincerity;

import java.io.File;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.threecrickets.scripturian.LanguageManager;

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

		//
		// Look for container in this order:
		//
		// 1. '--container=' switch
		// 2. 'sincerity.container' JVM property
		// 3. 'SINCERITY_CONTAINER' environment variable
		// 4. Search up filesystem tree from current path
		//

		String container = properties.get( "container" );
		if( container == null )
		{
			container = System.getProperty( "sincerity.container" );
			if( container == null )
				container = System.getenv( "SINCERITY_CONTAINER" );
		}

		File containerRootDir = null;
		if( container != null )
		{
			containerRootDir = new File( container ).getCanonicalFile();
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

		overwrite = "true".equals( properties.get( "overwrite" ) );

		this.container = new Container( containerRootDir, debugLevel );

		System.out.println( "Using Sincerity container at: " + containerRootDir );
	}

	//
	// Runnable
	//

	public void run()
	{
		if( statements.isEmpty() )
			return;

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
		if( "create".equals( command ) )
		{
			String template;
			if( statement.length < 2 )
				template = "default";
			else
				template = statement[1];

			System.out.println( template );
		}
		else if( "resolve".equals( command ) )
		{
			container.getDependencies().resolve();
		}
		else if( "install".equals( command ) )
		{
			container.getDependencies().resolve();
			container.getDependencies().install( overwrite );
		}
		else if( "unpack".equals( command ) )
		{
			String name;
			if( statement.length < 2 )
				name = null;
			else
				name = statement[1];

			if( name == null )
				container.getDependencies().getPackages().unpack( overwrite );
			else
			{
				Package pack = container.getDependencies().getPackages().get( name );
				if( pack == null )
					throw new Exception( "Unknown package: " + name );
				pack.unpack( overwrite );
			}
		}
		else if( "dependencies".equals( command ) )
		{
			container.getDependencies().getResolvedDependencies().printTree( new OutputStreamWriter( System.out ) );
		}
		else if( "artifacts".equals( command ) )
		{
			container.getDependencies().getResolvedDependencies().printArtifacts( new OutputStreamWriter( System.out ) );
		}
		else if( "clean".equals( command ) )
		{
			container.getDependencies().clean();
		}
		else if( "prune".equals( command ) )
		{
			container.getDependencies().prune();
		}
		else if( "reset".equals( command ) )
		{
			container.getDependencies().reset();
		}
		else if( "add".equals( command ) )
		{
			if( statement.length < 3 )
				throw new Exception( "'" + command + "' command requires: [group] [name] [[version]]" );

			String organisation = statement[1];
			String name = statement[2];
			String revision;
			if( statement.length < 4 )
				revision = "latest.integration";
			else
				revision = statement[3];

			if( !container.getDependencies().add( organisation, name, revision ) )
				System.err.println( "Dependency already in container: " + organisation + ":" + name + ":" + revision );
		}
		else if( "remove".equals( command ) )
		{
			if( statement.length < 4 )
				throw new Exception( "'" + command + "' command requires: [group] [name] [version]" );

			String organisation = statement[1];
			String name = statement[2];
			String revision = statement[3];

			if( !container.getDependencies().remove( organisation, name, revision ) )
				System.err.println( "Dependency was not in container: " + organisation + ":" + name + ":" + revision );
		}
		else if( "use".equals( command ) )
		{
			if( statement.length < 4 )
				throw new Exception( "'" + command + "' command requires: [section] [type] [name] ..." );

			String section = statement[1];
			String type = statement[2];
			String name = statement[3];

			if( "maven".equals( type ) || "ibiblio".equals( type ) )
			{
				if( statement.length < 5 )
					throw new Exception( "'" + command + " [section] " + type + " [name]' command also requires: [url]" );

				String url = statement[4];

				if( !container.getRepositories().addIbiblio( section, name, url ) )
					System.err.println( "Repository already in use: " + section + ":" + name );
			}
			else if( "pypi".equals( type ) || "python".equals( type ) )
			{
				if( statement.length < 5 )
					throw new Exception( "'" + command + " [section] " + type + " [name]' command also requires: [url]" );

				String url = statement[4];

				if( !container.getRepositories().addPyPi( section, name, url ) )
					System.err.println( "Repository already in use: " + section + ":" + name );
			}
			else
				System.err.println( "Unknown repository type: " + type );
		}
		else if( "unuse".equals( command ) )
		{
			if( statement.length < 3 )
				throw new Exception( "'" + command + "' command requires: [section] [name]" );

			String section = statement[1];
			String name = statement[2];

			if( !container.getRepositories().remove( section, name ) )
				System.err.println( "Repository was not in use: " + section + ":" + name );
		}
		else if( "main".equals( command ) )
		{
			if( statement.length < 2 )
				throw new Exception( "'" + command + "' command requires: [main class name] ..." );

			String mainClassName = statement[1];
			String[] mainArguments = new String[statement.length - 2];
			System.arraycopy( statement, 2, mainArguments, 0, mainArguments.length );

			main( mainClassName, mainArguments );
		}
		else if( "execute".equals( command ) )
		{
			if( statement.length < 2 )
			{
				System.err.println( "'" + command + "' command requires: [uri] ..." );
				return;
			}

			String[] executeArguments = new String[statement.length - 1];
			System.arraycopy( statement, 1, executeArguments, 0, executeArguments.length );

			System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, new File( container.getRoot(), "cache" ).getPath() );
			main( "com.threecrickets.scripturian.Scripturian", executeArguments );
		}
		else
			throw new Exception( "Unknown command: " + command );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final HashSet<String> switches = new HashSet<String>();

	private final ArrayList<ArrayList<String>> statements = new ArrayList<ArrayList<String>>();

	private final HashMap<String, String> properties = new HashMap<String, String>();

	private final boolean overwrite;

	private final Container container;

	private void main( String className, String[] arguments ) throws Exception
	{
		Class<?> theClass = container.getDependencies().getClassLoader().loadClass( className );
		Method mainMethod = theClass.getMethod( "main", String[].class );
		mainMethod.invoke( null, (Object) arguments );
	}
}

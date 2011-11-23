package com.threecrickets.sincerity;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.ivy.core.module.id.ModuleRevisionId;

import com.threecrickets.sincerity.Dependencies.Node;

public class Sincerity implements Runnable
{
	//
	// Main
	//

	public static void main( String[] arguments ) throws Exception
	{
		Sincerity sincerity = new Sincerity( arguments );
		sincerity.run();
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

		String root = properties.get( "root" );
		File rootFile = null;
		if( root != null )
			rootFile = new File( root ).getCanonicalFile();

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
			}
		}

		container = new Container( rootFile, debugLevel );

		overwrite = "true".equals( properties.get( "overwrite" ) );
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
		if( "resolve".equals( command ) )
		{
			container.getDependencies().resolve( overwrite );
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
			String organisation = statement[1];
			String name = statement[2];
			String revision = statement[3];

			if( !container.getDependencies().remove( organisation, name, revision ) )
				System.err.println( "Dependency was not in container: " + organisation + ":" + name + ":" + revision );
		}
		else if( "use".equals( command ) )
		{
			String type = statement[2];
			if( "maven".equals( type ) || "ibiblio".equals( type ) )
			{
				String section = statement[1];
				String name = statement[3];
				String url = statement[4];

				if( !container.getRepositories().addIbiblio( section, name, url ) )
					System.err.println( "Repository already in use: " + section + ":" + name );
			}
			else if( "pypi".equals( type ) || "python".equals( type ) )
			{
				String section = statement[1];
				String name = statement[3];
				String url = statement[4];

				if( !container.getRepositories().addPyPi( section, name, url ) )
					System.err.println( "Repository already in use: " + section + ":" + name );
			}
			else
				System.err.println( "Unknown repository type: " + type );
		}
		else if( "unuse".equals( command ) )
		{
			String section = statement[1];
			String name = statement[2];

			if( !container.getRepositories().remove( section, name ) )
				System.err.println( "Repository was not in use: " + section + ":" + name );
		}
		else if( "bootstrap".equals( command ) )
		{
			String mainClassName = statement[1];
			String[] mainArguments = new String[statement.length - 2];
			System.arraycopy( statement, 2, mainArguments, 0, mainArguments.length );

			String resolve = properties.get( "resolve" );
			if( !"false".equals( resolve ) )
				container.getDependencies().resolve( overwrite );

			Class<?> mainClass = container.getDependencies().getClassLoader().loadClass( mainClassName );
			Method mainMethod = mainClass.getMethod( "main", String[].class );
			mainMethod.invoke( null, (Object) mainArguments );
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
					System.err.println( "Unknown package: " + name );
				else
					pack.unpack( overwrite );
			}
		}
		else if( "list".equals( command ) )
		{
			for( Node node : container.getDependencies().getDescriptorTree() )
				printDependencies( node, 0 );
		}
		else
		{
			System.err.println( "Unknown command: " + command );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final HashSet<String> switches = new HashSet<String>();

	private final ArrayList<ArrayList<String>> statements = new ArrayList<ArrayList<String>>();

	private final HashMap<String, String> properties = new HashMap<String, String>();

	private final boolean overwrite;

	private final Container container;

	private static void printDependencies( Node node, int indent )
	{
		for( int i = 0; i < indent; i++ )
			System.out.print( "    " );
		ModuleRevisionId id = node.descriptor.getModuleRevisionId();
		System.out.println( id.getOrganisation() + " " + id.getName() + " " + id.getRevision() );
		for( Node child : node.children )
			printDependencies( child, indent + 1 );
	}
}

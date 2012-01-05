package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.sincerity.exception.AmbiguousCommandException;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.RebootException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.FileUtil;
import com.threecrickets.sincerity.internal.NativeUtil;

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

	//
	// Main
	//

	public static void main( String[] arguments )
	{
		try
		{
			Sincerity sincerity = new Sincerity( arguments, getCurrent() );
			sincerity.run();
		}
		catch( SincerityException x )
		{
			System.err.println( x.getMessage() );
			System.exit( 1 );
		}
	}

	//
	// Construction
	//

	public Sincerity( String[] arguments ) throws SincerityException
	{
		this( arguments, null );
	}

	public Sincerity( String[] arguments, Sincerity sincerity ) throws SincerityException
	{
		if( sincerity != null )
		{
			home = sincerity.home;
			container = sincerity.container;
			containerRoot = sincerity.containerRoot;
			plugins = sincerity.plugins;
			out = sincerity.out;
			err = sincerity.err;
			verbosity = sincerity.verbosity;
		}
		commands = parseCommands( arguments );
		threadLocal.set( this );
	}

	//
	// Attributes
	//

	public File getHome() throws SincerityException
	{
		if( home == null )
			home = Bootstrap.getHome();
		return home;
	}

	public File getHomeFile( String... parts ) throws SincerityException
	{
		File file = getHome();
		for( String part : parts )
			file = new File( file, part );
		return file;
	}

	public int getVerbosity()
	{
		if( verbosity == null )
		{
			verbosity = (Integer) Bootstrap.getAttributes().get( "com.threecrickets.sincerity.verbosity" );
			if( verbosity == null )
			{
				verbosity = 1;
				Bootstrap.getAttributes().put( "com.threecrickets.sincerity.verbosity", verbosity );
			}
		}

		return verbosity;
	}

	public void setVerbsotiy( int verbosity )
	{
		this.verbosity = verbosity;
		Bootstrap.getAttributes().put( "com.threecrickets.sincerity.verbosity", this.verbosity );
	}

	public PrintWriter getOut()
	{
		if( out == null )
		{
			out = (PrintWriter) Bootstrap.getAttributes().get( "com.threecrickets.sincerity.out" );
			if( out == null )
			{
				out = new PrintWriter( new OutputStreamWriter( System.out ), true );
				Bootstrap.getAttributes().put( "com.threecrickets.sincerity.out", out );
			}
		}

		return out;
	}

	public void setOut( Writer out )
	{
		this.out = out instanceof PrintWriter ? (PrintWriter) out : new PrintWriter( out, true );
		Bootstrap.getAttributes().put( "com.threecrickets.sincerity.out", this.out );
	}

	public PrintWriter getErr()
	{
		if( err == null )
		{
			err = (PrintWriter) Bootstrap.getAttributes().get( "com.threecrickets.sincerity.err" );
			if( err == null )
			{
				err = new PrintWriter( new OutputStreamWriter( System.err ), true );
				Bootstrap.getAttributes().put( "com.threecrickets.sincerity.err", err );
			}
		}

		return err;
	}

	public void setErr( Writer err )
	{
		this.err = err instanceof PrintWriter ? (PrintWriter) err : new PrintWriter( err, true );
		Bootstrap.getAttributes().put( "sincerity.err", this.err );
	}

	public File getContainerRoot() throws SincerityException
	{
		if( containerRoot == null )
		{
			containerRoot = (File) Bootstrap.getAttributes().get( "com.threecrickets.sincerity.containerRoot" );
			if( containerRoot == null )
			{
				containerRoot = findContainerRoot();
				Bootstrap.getAttributes().put( "com.threecrickets.sincerity.containerRoot", containerRoot );
			}
		}
		return containerRoot;
	}

	public void setContainerRoot( File containerRoot ) throws SincerityException
	{
		try
		{
			Bootstrap.getAttributes().put( "com.threecrickets.sincerity.containerRoot", containerRoot.getCanonicalFile() );
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not access container root: " + containerRoot );
		}

		reboot();
	}

	public Container getContainer() throws SincerityException
	{
		if( container == null )
		{
			File containerRoot = getContainerRoot();

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

			container = new Container( this, containerRoot, debugLevel );

			if( getVerbosity() >= 2 )
				getOut().println( "Using Sincerity container at: " + containerRoot );
		}

		return container;
	}

	//
	// Operations
	//

	public void createContainer( File containerRoot, File templateDir ) throws SincerityException
	{
		if( containerRoot.exists() )
		{
			if( new File( containerRoot, Container.SINCERITY_DIR ).exists() )
			{
				if( getVerbosity() >= 1 )
					getOut().println( "The path is already a Sincerity container: " + containerRoot );
				setContainerRoot( containerRoot );
				return;
			}
		}

		if( !templateDir.isDirectory() )
			throw new SincerityException( "Could not find container template: " + templateDir );

		containerRoot.mkdirs();
		new File( containerRoot, Container.SINCERITY_DIR ).mkdirs();
		for( File file : templateDir.listFiles() )
		{
			try
			{
				FileUtil.copyRecursive( file, containerRoot );
			}
			catch( IOException x )
			{
				throw new SincerityException( "Could not copy file from template to container: " + file );
			}
		}

		setContainerRoot( containerRoot );
	}

	public List<Command> parseCommands( String... arguments )
	{
		LinkedList<Command> commands = new LinkedList<Command>();

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
					command = new Command( argument, isGreedy, this );
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
		boolean isGreedy;
		if( name.endsWith( Command.GREEDY_POSTFIX ) )
		{
			name = name.substring( 0, name.length() - Command.GREEDY_POSTFIX_LENGTH );
			isGreedy = true;
		}
		else
			isGreedy = false;

		Command command = new Command( name, isGreedy, this );
		for( String argument : arguments )
			command.rawArguments.add( argument );
		run( command );
	}

	public void reboot() throws SincerityException
	{
		// Convert remaining commands back into arguments
		ArrayList<String> arguments = new ArrayList<String>();
		for( Iterator<Command> i = commands.iterator(); i.hasNext(); )
		{
			Command command = i.next();
			for( String argument : command.toArguments() )
				arguments.add( argument );
			if( i.hasNext() )
				arguments.add( Command.COMMANDS_SEPARATOR );
		}

		if( arguments.isEmpty() )
			return;

		try
		{
			// Go native!
			File nativeDir = getContainer().getLibrariesFile( "native" );
			if( nativeDir.isDirectory() )
				NativeUtil.addNativePath( nativeDir );

			// Boostrap into container
			Bootstrap.bootstrap( getContainer().getRoot(), getContainer().getDependencies().createBootstrap(), arguments.toArray( new String[arguments.size()] ) );
		}
		catch( Exception x )
		{
			throw new SincerityException( "Could not bootstrap", x );
		}

		throw new RebootException();
	}

	//
	// Runnable
	//

	public void run()
	{
		if( commands.isEmpty() )
			commands.add( new Command( "help" + Command.PLUGIN_COMMAND_SEPARATOR + "help", false, this ) );

		try
		{
			while( !commands.isEmpty() )
			{
				Command command = commands.remove( 0 );
				run( command );
			}
		}
		catch( RebootException x )
		{
			// This means that the run has continued in a different bootstrap
		}
		catch( SincerityException x )
		{
			getErr().println( x.getMessage() );
			x.printStackTrace( getErr() );
			System.exit( 1 );
		}
		catch( Throwable x )
		{
			x.printStackTrace( getErr() );
			System.exit( 1 );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final ThreadLocal<Sincerity> threadLocal = new ThreadLocal<Sincerity>();

	private final List<Command> commands;

	private File home;

	private File containerRoot;

	private Container container;

	private Plugins plugins;

	private PrintWriter out;

	private PrintWriter err;

	private Integer verbosity;

	private Plugins getPlugins() throws SincerityException
	{
		try
		{
			return getContainer().getDependencies().getPlugins();
		}
		catch( NoContainerException x )
		{
			if( plugins == null )
				plugins = new Plugins( this );

			return plugins;
		}
	}

	private static File findContainerRoot() throws SincerityException
	{
		//
		// Look for container in this order:
		//
		// 1. 'sincerity.container' JVM property
		// 2. 'SINCERITY_CONTAINER' environment variable
		// 3. Search up filesystem tree from current path
		//

		File containerRoot = null;

		String path = System.getProperty( CONTAINER_PROPERTY );
		if( path == null )
			path = System.getenv( CONTAINER_ENV );
		if( path != null )
			containerRoot = new File( path );

		if( containerRoot != null )
		{
			try
			{
				containerRoot = containerRoot.getCanonicalFile();
			}
			catch( IOException x )
			{
				throw new SincerityException( "Could not access container root: " + containerRoot, x );
			}

			if( !containerRoot.exists() )
				throw new SincerityException( "Specified root path for the Sincerity container does not point anywhere: " + containerRoot );
			if( !containerRoot.isDirectory() )
				throw new SincerityException( "Specified root path for the Sincerity container does not point to a directory: " + containerRoot );
			File sincerityDir = new File( containerRoot, Container.SINCERITY_DIR );
			if( !sincerityDir.isDirectory() )
				throw new SincerityException( "Specified root path for the Sincerity container does not point to a valid container: " + containerRoot );
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
				throw new SincerityException( "Could not access current directory", x );
			}
			containerRoot = currentDir;
			while( true )
			{
				File sincerityDir = new File( containerRoot, Container.SINCERITY_DIR );
				if( sincerityDir.isDirectory() )
				{
					// Found it!
					break;
				}
				containerRoot = containerRoot.getParentFile();
				if( containerRoot == null )
					throw new NoContainerException( "Could not find a Sincerity container for the current directory: " + currentDir );
				try
				{
					containerRoot = containerRoot.getCanonicalFile();
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not access container root: " + containerRoot, x );
				}
			}
		}

		return containerRoot;
	}
}

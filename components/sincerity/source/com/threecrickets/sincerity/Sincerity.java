/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.sincerity.exception.AmbiguousCommandException;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.RebootException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.internal.FileUtil;
import com.threecrickets.sincerity.internal.NativeUtil;
import com.threecrickets.sincerity.internal.StringUtil;
import com.threecrickets.sincerity.plugin.gui.Frame;

/**
 * This is the highest level instance for the Sincerity runtime.
 * <p>
 * While strictly speaking it is a singleton, several Sincerity instances may
 * coexist via the {@link Bootstrap} mechanism. This is in order to allow
 * Sincerity to "reboot" itself using a new classpath in case the
 * {@link Container} is changed. See {@link #reboot()}.
 * <p>
 * The main role of this class is to manage the {@link Container} singleton.
 * Indeed, Sincerity can run without a container, and this class can be used to
 * create it.
 * <p>
 * This is also where you can access the "system" plugins, those that are part
 * of the Sincerity base installation and are not associated with any container.
 * See {@link #getPlugins()}.
 * <p>
 * Additionally, this class manages the general runtime environment and
 * interface for all of Sincerity. This includes both the command line interface
 * (CLI) and the graphical user interface (GUI).
 * 
 * @author Tal Liron
 */
public class Sincerity implements Runnable
{
	//
	// Constants
	//

	public static final String CONTAINER_PROPERTY = "sincerity.container.root";

	public static final String CONTAINER_ENV = "SINCERITY_CONTAINER";

	public static final String CONTAINER_ATTRIBUTE = "com.threecrickets.sincerity.containerRoot";

	public static final String DEBUG_PROPERTY = "sincerity.debug";

	public static final String DEBUG_ENV = "SINCERITY_DEBUG";

	//
	// Static attributes
	//

	/**
	 * The Sincerity instance running in the current thread.
	 * 
	 * @return The Sincerity instance
	 */
	public static Sincerity getCurrent()
	{
		return threadLocal.get();
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> getVersion()
	{
		Map<String, String> version = (Map<String, String>) Bootstrap.getAttributes().get( "com.threecrickets.sincerity.version" );
		if( version == null )
		{
			Properties properties = new Properties();
			InputStream stream = Sincerity.class.getResourceAsStream( "version.conf" );
			try
			{
				properties.load( stream );
			}
			catch( IOException x )
			{
			}
			finally
			{
				try
				{
					stream.close();
				}
				catch( IOException x )
				{
				}
			}
			version = new HashMap<String, String>();
			for( Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); )
			{
				Object name = e.nextElement();
				version.put( name.toString(), properties.getProperty( name.toString() ) );
			}
			Map<String, String> existing = (Map<String, String>) Bootstrap.getAttributes().putIfAbsent( "com.threecrickets.sincerity.version", version );
			if( existing != null )
				version = existing;
		}
		return version;
	}

	//
	// Main
	//

	public static void main( String[] arguments )
	{
		boolean started = Bootstrap.getAttributes().get( "com.threecrickets.sincerity.started" ) != null;
		try
		{
			Sincerity sincerity = new Sincerity( arguments, getCurrent() );

			if( !started && sincerity.commands.isEmpty() )
				sincerity.commands.add( new Command( "gui", "gui", false, sincerity ) );

			if( !started )
				Bootstrap.getAttributes().put( "com.threecrickets.sincerity.started", true );

			sincerity.run();
		}
		catch( SincerityException x )
		{
			System.err.println( x.getMessage() );
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
		Integer verbosity = (Integer) Bootstrap.getAttributes().get( "com.threecrickets.sincerity.verbosity" );
		if( verbosity == null )
		{
			verbosity = 1;
			Bootstrap.getAttributes().put( "com.threecrickets.sincerity.verbosity", verbosity );
		}

		return verbosity;
	}

	public void setVerbosity( int verbosity )
	{
		Bootstrap.getAttributes().put( "com.threecrickets.sincerity.verbosity", verbosity );
	}

	public PrintWriter getOut()
	{
		PrintWriter out = (PrintWriter) Bootstrap.getAttributes().get( "com.threecrickets.sincerity.out" );
		if( out == null )
		{
			out = new PrintWriter( new OutputStreamWriter( System.out ), true );
			PrintWriter existing = (PrintWriter) Bootstrap.getAttributes().putIfAbsent( "com.threecrickets.sincerity.out", out );
			if( existing != null )
				out = existing;
		}

		return out;
	}

	public void setOut( Writer out )
	{
		if( !( out instanceof PrintWriter ) )
			out = new PrintWriter( out, true );
		Bootstrap.getAttributes().put( "com.threecrickets.sincerity.out", out );
	}

	public PrintWriter getErr()
	{
		PrintWriter err = (PrintWriter) Bootstrap.getAttributes().get( "com.threecrickets.sincerity.err" );
		if( err == null )
		{
			err = new PrintWriter( new OutputStreamWriter( System.err ), true );
			PrintWriter existing = (PrintWriter) Bootstrap.getAttributes().putIfAbsent( "com.threecrickets.sincerity.err", err );
			if( existing != null )
				err = existing;
		}

		return err;
	}

	public void setErr( Writer err )
	{
		if( !( err instanceof PrintWriter ) )
			err = new PrintWriter( err, true );
		Bootstrap.getAttributes().put( "com.threecrickets.sincerity.err", err );
	}

	public File getContainerRoot() throws SincerityException
	{
		if( containerRoot == null )
		{
			containerRoot = (File) Bootstrap.getAttributes().get( CONTAINER_ATTRIBUTE );
			if( containerRoot == null )
				setContainerRoot( findContainerRoot() );
		}
		return containerRoot;
	}

	public void setContainerRoot( File containerRoot ) throws SincerityException
	{
		try
		{
			File canonicalContainerRoot = containerRoot.getCanonicalFile();
			Bootstrap.getAttributes().put( CONTAINER_ATTRIBUTE, canonicalContainerRoot );
			System.setProperty( CONTAINER_PROPERTY, canonicalContainerRoot.toString() );
			this.containerRoot = canonicalContainerRoot;

			// Depends on the container root
			container = null;

			reboot();
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not access container root: " + containerRoot );
		}
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

	public Plugins getPlugins() throws SincerityException
	{
		try
		{
			return getContainer().getPlugins();
		}
		catch( NoContainerException x )
		{
			if( plugins == null )
				plugins = new Plugins( this );

			return plugins;
		}
	}

	public Frame getFrame()
	{
		return frame;
	}

	public void setFrame( Frame frame )
	{
		this.frame = frame;
	}

	public List<Template> getTemplates() throws SincerityException
	{
		ArrayList<Template> templates = new ArrayList<Template>();
		File templatesDir = new File( getHome(), "templates" );
		if( templatesDir.isDirectory() )
		{
			for( File templateDir : templatesDir.listFiles() )
			{
				if( templateDir.isDirectory() )
					templates.add( new Template( templateDir ) );
			}
		}
		return templates;
	}

	//
	// Operations
	//

	public void createContainer( File containerRoot, File templateDir, boolean force ) throws SincerityException
	{
		if( !force )
		{
			if( containerRoot.isDirectory() )
			{
				if( new File( containerRoot, Container.SINCERITY_DIR ).isDirectory() )
				{
					if( getVerbosity() >= 1 )
						getOut().println( "The folder is already a Sincerity container: " + containerRoot );
					setContainerRoot( containerRoot );
					return;
				}
				else
					throw new SincerityException( "The folder already exists: " + containerRoot );
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

					if( argument.equals( "--help" ) || argument.equals( "-h" ) )
						argument = "help" + Command.PLUGIN_COMMAND_SEPARATOR + "help";

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
			Plugin1 plugin = getPlugins().get( command.plugin );
			if( plugin == null )
				throw new UnknownCommandException( command );
			plugin.run( command );
		}
		else
		{
			ArrayList<Plugin1> plugins = new ArrayList<Plugin1>();
			for( Plugin1 plugin : getPlugins().values() )
			{
				if( Arrays.asList( plugin.getCommands() ).contains( command.getName() ) )
					plugins.add( plugin );
			}

			int size = plugins.size();
			if( size == 1 )
			{
				Plugin1 plugin = plugins.get( 0 );
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

	public void skip( Command command )
	{
		commands.remove( command );
	}

	public void reboot() throws SincerityException
	{
		// Convert commands into arguments to be re-parsed
		ArrayList<String> arguments = new ArrayList<String>();
		for( Iterator<Command> i = commands.iterator(); i.hasNext(); )
		{
			Command c = i.next();
			for( String argument : c.toArguments() )
				arguments.add( argument );
			if( i.hasNext() )
				arguments.add( Command.COMMANDS_SEPARATOR );
		}

		try
		{
			// Go native!
			File nativeDir = getContainer().getLibrariesFile( "native" );
			NativeUtil.addNativePath( nativeDir );

			// Bootstrap into container
			getContainer().getBootstrap().bootstrap( arguments.toArray( new String[arguments.size()] ) );
		}
		catch( Exception x )
		{
			throw new SincerityException( "Could not bootstrap", x );
		}

		throw new RebootException();
	}

	public void printStackTrace( Throwable x )
	{
		getErr().println( StringUtil.joinStackTrace( x ) );
	}

	//
	// Runnable
	//

	public void run()
	{
		try
		{
			while( !commands.isEmpty() )
			{
				Command command = commands.get( 0 );
				run( command );
				commands.remove( command );
			}
		}
		catch( RebootException x )
		{
			// This means that the run has continued in a different bootstrap
		}
		catch( SincerityException x )
		{
			if( getVerbosity() >= 2 )
				printStackTrace( x );
			else
				getErr().println( x.getMessage() );
		}
		catch( Throwable x )
		{
			printStackTrace( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected final List<Command> commands;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final ThreadLocal<Sincerity> threadLocal = new ThreadLocal<Sincerity>();

	private File home;

	private File containerRoot;

	private Container container;

	private Plugins plugins;

	private Frame frame;

	private static File findContainerRoot() throws SincerityException
	{
		//
		// Look for container in this order:
		//
		// 1. 'sincerity.container.root' JVM property
		// 2. 'SINCERITY_CONTAINER' environment variable
		// 3. Search up filesystem tree from current path
		//

		try
		{
			File containerRoot = null;

			String path = System.getProperty( CONTAINER_PROPERTY );
			if( path == null )
				path = System.getenv( CONTAINER_ENV );
			if( path != null )
				containerRoot = new File( path ).getCanonicalFile();

			if( containerRoot != null )
			{
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
				File currentDir = new File( "." ).getCanonicalFile();
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
				}
			}

			return containerRoot;
		}
		catch( IOException x )
		{
			throw new SincerityException( "I/O error searching for Sincerity container" );
		}
	}
}

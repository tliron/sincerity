/**
 * Copyright 2011-2015 Three Crickets LLC.
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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.sincerity.exception.AmbiguousCommandException;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.RebootException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.ivy.IvyContainer;
import com.threecrickets.sincerity.plugin.gui.Frame;
import com.threecrickets.sincerity.util.IoUtil;
import com.threecrickets.sincerity.util.NativeUtil;
import com.threecrickets.sincerity.util.Pipe;
import com.threecrickets.sincerity.util.StringUtil;

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

	public static final String DEBUG_PROPERTY = "sincerity.debug";

	public static final String DEBUG_ENV = "SINCERITY_DEBUG";

	public static final String VERSION_ATTRIBUTE = "com.threecrickets.sincerity.version";

	public static final String CONTAINER_ROOT_ATTRIBUTE = "com.threecrickets.sincerity.containerRoot";

	public static final String STARTED_ATTRIBUTE = "com.threecrickets.sincerity.started";

	public static final String VERBOSITY_ATTRIBUTE = "com.threecrickets.sincerity.verbosity";

	public static final String OUT_ATTRIBUTE = "com.threecrickets.sincerity.out";

	public static final String ERR_ATTRIBUTE = "com.threecrickets.sincerity.err";

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

	//
	// Main
	//

	/**
	 * Executes a Sincerity command line, defaulting to the "shell:console"
	 * command if no commands are given.
	 * 
	 * @param arguments
	 *        The command line
	 */
	public static void main( String[] arguments )
	{
		boolean started = Bootstrap.getAttributes().get( STARTED_ATTRIBUTE ) != null;
		try
		{
			Sincerity sincerity = new Sincerity( arguments, getCurrent() );

			if( !started && sincerity.commands.isEmpty() )
				sincerity.commands.add( new Command( "shell", "console", false, sincerity ) );

			if( !started )
				Bootstrap.getAttributes().put( STARTED_ATTRIBUTE, true );

			sincerity.run();
		}
		catch( SincerityException x )
		{
			System.err.println( "Error: " + x.getMessage() );
		}
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param arguments
	 *        The command line
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Sincerity( String[] arguments ) throws SincerityException
	{
		this( arguments, null );
	}

	/**
	 * Cloning constructor.
	 * 
	 * @param arguments
	 *        The command line
	 * @param sincerity
	 *        The instance to clone
	 * @throws SincerityException
	 *         In case of an error
	 */
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

	/**
	 * Sincerity version information (cached as a bootstrap attribute).
	 * <p>
	 * Taken from the "version.conf" resource.
	 * 
	 * @return Version information
	 */
	@SuppressWarnings("unchecked")
	public Map<String, String> getVersion()
	{
		Map<String, String> version = (Map<String, String>) Bootstrap.getAttributes().get( VERSION_ATTRIBUTE );
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
			Map<String, String> existing = (Map<String, String>) Bootstrap.getAttributes().putIfAbsent( VERSION_ATTRIBUTE, version );
			if( existing != null )
				version = existing;
		}
		return version;
	}

	/**
	 * The Sincerity home directory
	 * 
	 * @return The home directory
	 * @throws SincerityException
	 *         In case of an error
	 */
	public File getHome() throws SincerityException
	{
		if( home == null )
			home = Bootstrap.getHome();
		return home;
	}

	/**
	 * Constructs an absolute path from the home directory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 * @throws SincerityException
	 *         In case of an error
	 */
	public File getHomeFile( String... parts ) throws SincerityException
	{
		File file = getHome();
		for( String part : parts )
			file = new File( file, part );
		return file;
	}

	/**
	 * If the file is under to the root directory, returns a relative path.
	 * 
	 * @param file
	 *        The absolute file
	 * @return The relative path
	 * @see #getRelativeHomePath(File)
	 * @throws SincerityException
	 *         In case of an error
	 */
	public String getRelativeHomePath( File file ) throws SincerityException
	{
		return getRelativeHomePath( file.getAbsolutePath() );
	}

	/**
	 * If the path is under to the root directory, returns a relative path.
	 * 
	 * @param path
	 *        The absolute path
	 * @return The relative path
	 * @see #getRelativeHomePath(File)
	 * @throws SincerityException
	 *         In case of an error
	 */
	public String getRelativeHomePath( String path ) throws SincerityException
	{
		String root = getHome().getPath();
		if( path.startsWith( root ) )
			path = path.substring( root.length() + 1 );
		return path;
	}

	/**
	 * @return The verbosity level
	 * @see #setVerbosity(int)
	 */
	public int getVerbosity()
	{
		Integer verbosity = (Integer) Bootstrap.getAttributes().get( VERBOSITY_ATTRIBUTE );
		if( verbosity == null )
		{
			verbosity = 1;
			Bootstrap.getAttributes().put( VERBOSITY_ATTRIBUTE, verbosity );
		}

		return verbosity;
	}

	/**
	 * Verbosity is only used to control messages to standard output and
	 * standard error
	 * <p>
	 * It is interpreted individually by individual commands, though 0 usually
	 * means "silent," 1 means "only important messages" and 2 means
	 * "quite chatty." Higher values usually include more minute debugging
	 * information.
	 * 
	 * @param verbosity
	 *        The verbosity level
	 * @see #getVerbosity()
	 */
	public void setVerbosity( int verbosity )
	{
		Bootstrap.getAttributes().put( VERBOSITY_ATTRIBUTE, verbosity );
	}

	/**
	 * @return The print writer
	 * @see #setOut(Writer)
	 */
	public PrintWriter getOut()
	{
		PrintWriter out = (PrintWriter) Bootstrap.getAttributes().get( OUT_ATTRIBUTE );
		if( out == null )
		{
			out = new PrintWriter( new OutputStreamWriter( System.out ), true );
			PrintWriter existing = (PrintWriter) Bootstrap.getAttributes().putIfAbsent( OUT_ATTRIBUTE, out );
			if( existing != null )
				out = existing;
		}

		return out;
	}

	/**
	 * The standard output writer.
	 * <p>
	 * Will be wrapped in a {@link PrintWriter} if it's not already one.
	 * 
	 * @param out
	 *        The writer
	 * @see #getOut()
	 */
	public void setOut( Writer out )
	{
		if( !( out instanceof PrintWriter ) )
			out = new PrintWriter( out, true );
		Bootstrap.getAttributes().put( OUT_ATTRIBUTE, out );
	}

	/**
	 * @return The print writer
	 * @see #setErr(Writer)
	 */
	public PrintWriter getErr()
	{
		PrintWriter err = (PrintWriter) Bootstrap.getAttributes().get( ERR_ATTRIBUTE );
		if( err == null )
		{
			err = new PrintWriter( new OutputStreamWriter( System.err ), true );
			PrintWriter existing = (PrintWriter) Bootstrap.getAttributes().putIfAbsent( ERR_ATTRIBUTE, err );
			if( existing != null )
				err = existing;
		}

		return err;
	}

	/**
	 * The standard error writer.
	 * <p>
	 * Will be wrapped in a {@link PrintWriter} if it's not already one.
	 * 
	 * @param err
	 *        The writer
	 */
	public void setErr( Writer err )
	{
		if( !( err instanceof PrintWriter ) )
			err = new PrintWriter( err, true );
		Bootstrap.getAttributes().put( ERR_ATTRIBUTE, err );
	}

	/**
	 * The root directory of the current container.
	 * 
	 * @return The canonical file
	 * @throws SincerityException
	 *         In case of an error
	 * @see #setContainerRoot(File)
	 */
	public File getContainerRoot() throws SincerityException
	{
		if( containerRoot == null )
		{
			containerRoot = (File) Bootstrap.getAttributes().get( CONTAINER_ROOT_ATTRIBUTE );
			if( containerRoot == null )
				setContainerRoot( findContainerRoot() );
		}
		return containerRoot;
	}

	/**
	 * Changes the current container by specifying a new root directory.
	 * <p>
	 * Causes a {@link #reboot()} if the current container is changed!
	 * 
	 * @param containerRoot
	 *        The container root
	 * @throws SincerityException
	 *         In case of an error
	 * @see #getContainerRoot()
	 */
	public void setContainerRoot( File containerRoot ) throws SincerityException
	{
		try
		{
			File canonicalContainerRoot = containerRoot.getCanonicalFile();

			// Make sure this is a new container root
			if( this.containerRoot == null )
				this.containerRoot = (File) Bootstrap.getAttributes().get( CONTAINER_ROOT_ATTRIBUTE );
			if( this.containerRoot != null )
				if( canonicalContainerRoot.equals( this.containerRoot ) )
					return;

			Bootstrap.getAttributes().put( CONTAINER_ROOT_ATTRIBUTE, canonicalContainerRoot );
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

	/**
	 * The current container.
	 * <p>
	 * Creates a new instance if not previously accessed.
	 *
	 * @param <RD>
	 *        The resolved dependency class
	 * @param <R>
	 *        The repositories class
	 * @return The container
	 * @throws SincerityException
	 *         In case of an error
	 */
	@SuppressWarnings("unchecked")
	public <RD extends ResolvedDependency, R extends Repositories> Container<RD, R> getContainer() throws SincerityException
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

			container = new IvyContainer( this, containerRoot, debugLevel );

			if( getVerbosity() >= 2 )
				getOut().println( "Using Sincerity container at: " + containerRoot );
		}

		return (Container<RD, R>) container;
	}

	/**
	 * The plugins.
	 * <p>
	 * If there is a current container, then we will include its classpath.
	 * Otherwise, we will only consider the Sincerity home plugins.
	 * 
	 * @return The plugins
	 * @throws SincerityException
	 *         In case of an error
	 */
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

	/**
	 * @return The GUI frame or null
	 * @see #setFrame(Frame)
	 */
	public Frame getFrame()
	{
		return frame;
	}

	/**
	 * The current open GUI frame.
	 * 
	 * @param frame
	 *        The GUI frame
	 * @see #getFrame()
	 */
	public void setFrame( Frame frame )
	{
		this.frame = frame;
	}

	/**
	 * The available templates (names of directory in the "/templates/"
	 * subdirectory of the Sincerity install).
	 * 
	 * @return The templates
	 * @throws SincerityException
	 *         In case of an error
	 */
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

	/**
	 * Creates a new container based on a template and sets it as the current
	 * container.
	 * <p>
	 * Unless force is true, will not copy any files if the directory already
	 * exists.
	 * 
	 * @param containerRoot
	 *        The container root directory
	 * @param templateDir
	 *        The template root directory
	 * @param force
	 *        True to force overriding of existing files
	 * @throws SincerityException
	 *         In case of an error
	 */
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
				IoUtil.copyRecursive( file, containerRoot );
			}
			catch( IOException x )
			{
				throw new SincerityException( "Could not copy file from template to container: " + file );
			}
		}

		setContainerRoot( containerRoot );
	}

	/**
	 * Runs a command line with the current set of plugs. Supports expanding
	 * shortcuts.
	 * 
	 * @param arguments
	 *        The command arguments
	 * @throws SincerityException
	 *         In case of an error
	 * @see #getPlugins()
	 */
	public void run( Object... arguments ) throws SincerityException
	{
		// Insert at beginning of current command line queue with an "until" tag
		String[] argumentsAsStrings = StringUtil.toStringArray( arguments );
		LinkedList<Command> newCommands = parseCommands( argumentsAsStrings );
		newCommands.add( Command.UNTIL );
		commands.addAll( 0, newCommands );
		if( getVerbosity() >= 3 )
			getOut().println( "Sincerity running: " + StringUtil.join( argumentsAsStrings, " " ) + "..." );
		run( true );
	}

	/**
	 * Reboots Sincerity without forcing a new container bootstrap.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 * @see #reboot(boolean)
	 */
	public void reboot() throws SincerityException
	{
		reboot( false );
	}

	/**
	 * Reboots Sincerity, resubmitting the current command queue to the new
	 * instance.
	 * 
	 * @param forceNewBootstrap
	 *        True to force the re-creation of the container's bootstrap
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void reboot( boolean forceNewBootstrap ) throws SincerityException
	{
		if( commands.isEmpty() )
			return;

		try
		{
			// Go native!
			File nativeDir = getContainer().getLibrariesFile( "native" );
			NativeUtil.addNativePath( nativeDir );

			if( getVerbosity() >= 3 )
			{
				ArrayList<String> arguments = unparseCommands( true );
				getOut().println( "Sincerity rebooting with command line: " + StringUtil.join( arguments, " " ) + "..." );
			}

			// Bootstrap into container
			ArrayList<String> arguments = unparseCommands( false );
			getContainer().getBootstrap( forceNewBootstrap ).bootstrap( arguments.toArray( new String[arguments.size()] ) );
		}
		catch( SincerityException x )
		{
			throw x;
		}
		catch( Exception x )
		{
			throw new SincerityException( "Could not bootstrap", x );
		}

		throw new RebootException();
	}

	/**
	 * Captures the standard output and standard error of a process.
	 * 
	 * @param process
	 *        The process
	 * @see #setOut(Writer)
	 * @see #setErr(Writer)
	 */
	public void captureOutput( Process process )
	{
		new Thread( new Pipe( new InputStreamReader( process.getInputStream() ), getOut() ) ).start();
		new Thread( new Pipe( new InputStreamReader( process.getErrorStream() ), getErr() ) ).start();
	}

	/**
	 * Dumps an exception's stack trace to standard error.
	 * 
	 * @param x
	 *        The exception
	 * @see #setErr(Writer)
	 */
	public void dumpStackTrace( Throwable x )
	{
		getErr().println( StringUtil.createHumanReadableStackTrace( x ) );
	}

	//
	// Runnable
	//

	public void run()
	{
		try
		{
			run( false );
		}
		catch( SincerityException x )
		{
			// Should never happen
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Removes a command from the current command queue.
	 * 
	 * @param command
	 *        The command
	 */
	protected void removeCommand( Command command )
	{
		commands.remove( command );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final ThreadLocal<Sincerity> threadLocal = new ThreadLocal<Sincerity>();

	/**
	 * The command queue.
	 */
	private final LinkedList<Command> commands;

	private File home;

	private File containerRoot;

	private Container<?, ?> container;

	private Plugins plugins;

	private Frame frame;

	/**
	 * Look for a container in this order:
	 * <p>
	 * 1. 'sincerity.container.root' JVM property<br />
	 * 2. 'SINCERITY_CONTAINER' environment variable<br />
	 * 3. Search up filesystem tree from current path
	 * 
	 * @return The canonical file
	 * @throws SincerityException
	 *         In case of an error
	 */
	private static File findContainerRoot() throws SincerityException
	{

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

	/**
	 * Parses a command line.
	 * <p>
	 * No validation is done of commands here, neither are shortcuts expanded.
	 * 
	 * @param arguments
	 *        The command line
	 * @return The commands
	 * @see #unparseCommands(boolean)
	 */
	private LinkedList<Command> parseCommands( String... arguments )
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

					// Special handling for --help
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

	/**
	 * Turns the current command line queue into raw arguments.
	 * 
	 * @param includeUntil
	 *        Whether to include {@link Command#UNTIL} instances
	 * @return The command line arguments
	 */
	private ArrayList<String> unparseCommands( boolean includeUntil )
	{
		ListIterator<Command> i;
		if( includeUntil )
			i = commands.listIterator();
		else
		{
			// Start after last "until" tag
			i = commands.listIterator( commands.size() );
			while( i.hasPrevious() )
			{
				Command c = i.previous();
				if( c == Command.UNTIL )
				{
					i.next();
					break;
				}
			}
		}

		ArrayList<String> arguments = new ArrayList<String>();
		while( i.hasNext() )
		{
			Command c = i.next();
			for( String argument : c.toArguments() )
				arguments.add( argument );
			if( i.hasNext() )
				arguments.add( Command.COMMANDS_SEPARATOR );
		}

		return arguments;
	}

	/**
	 * Runs the current command line queue with the current set of plugs.
	 * Supported expanding shortcuts.
	 * <p>
	 * When isManual is true (see {@link #run(String...)}, exceptions are thrown
	 * and the special {@link Command#UNTIL} tag is used to stop execution.
	 * <p>
	 * Otherwise, exceptions are never thrown, despite the checked exception
	 * declaration.
	 * 
	 * @param isManual
	 *        Whether we are running in manual mode
	 * @throws SincerityException
	 *         In case of an error
	 */
	private void run( boolean isManual ) throws SincerityException
	{
		Command command = null;
		try
		{
			while( !commands.isEmpty() )
			{
				command = commands.peek();

				// Check for special "until" tag
				if( command == Command.UNTIL )
				{
					commands.remove( command );
					if( isManual )
						// Stop!
						break;
					else
					{
						command = commands.peek();
						if( command == null )
							break;
					}
				}

				if( command.getName().startsWith( Shortcuts.SHORTCUT_PREFIX ) )
				{
					// Expands shortcuts into current command queue
					try
					{
						String[] shortcut = getContainer().getShortcuts().get( command.getName().substring( Shortcuts.SHORTCUT_PREFIX_LENGTH ) );
						LinkedList<Command> shortcutCommands = parseCommands( shortcut );
						commands.remove( command );
						commands.addAll( 0, shortcutCommands );
						command = commands.peek();
						if( command == null )
							break;
					}
					catch( NoContainerException x )
					{
					}
				}

				if( command.plugin != null )
				{
					// Plugin was provided
					Plugin1 plugin = getPlugins().get( command.plugin );
					if( plugin == null )
						throw new UnknownCommandException( command );

					plugin.run( command );
				}
				else
				{
					// Plugin was not provided, so try all plugins
					ArrayList<Plugin1> plugins = new ArrayList<Plugin1>();
					for( Plugin1 plugin : getPlugins().values() )
					{
						String[] commands = plugin.getCommands();
						if( ( commands != null ) && Arrays.asList( commands ).contains( command.getName() ) )
							plugins.add( plugin );
					}

					int size = plugins.size();
					if( size == 1 )
					{
						// Found unambiguous plugin
						Plugin1 plugin = plugins.get( 0 );
						command.plugin = plugin.getName();

						plugin.run( command );
					}
					else if( size > 1 )
					{
						throw new AmbiguousCommandException( command, plugins );
					}
					else
					{
						throw new UnknownCommandException( command );
					}
				}

				commands.remove( command );
			}
		}
		catch( RebootException x )
		{
			// This means that the run has continued in a different bootstrap
		}
		catch( SincerityException x )
		{
			commands.remove( command );
			if( isManual )
				throw x;
			else
			{
				if( getVerbosity() >= 2 )
					dumpStackTrace( x );
				else
					getErr().println( "Error: " + x.getMessage() );
			}
		}
		catch( Throwable x )
		{
			commands.remove( command );
			if( isManual )
				throw new SincerityException( "Something very bad happened!", x );
			else
				dumpStackTrace( x );
		}
	}
}

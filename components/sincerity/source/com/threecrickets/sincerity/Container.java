/**
 * configure() * Copyright 2011-2015 Three Crickets LLC.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.util.DefaultMessageLogger;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.ParserManager;
import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.dependencies.Module;
import com.threecrickets.sincerity.dependencies.Repositories;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.packaging.PackagingContext;
import com.threecrickets.sincerity.util.RootDirectory;
import com.threecrickets.sincerity.util.ScripturianUtil;

/**
 * The second highest level instance for the Sincerity runtime, after the
 * {@link Sincerity} class.
 * <p>
 * While strictly speaking it is a singleton, several Container instances may
 * coexist via the {@link Bootstrap} mechanism. This is in order to allow
 * Sincerity to "reboot" itself using a new classpath in case the
 * {@link Container} is changed. See {@link Sincerity#reboot()}.
 * <p>
 * Because the set of plugins in a container depends on its classpath, this is
 * also where you can access them, via {@link #getPlugins()}.
 * 
 * @param <M>
 *        The resolved dependency class
 * @param <R>
 *        The repositories class
 * @author Tal Liron
 */
public abstract class Container<M extends Module, R extends Repositories> extends RootDirectory
{
	//
	// Constants
	//

	public static final String SINCERITY_DIR = ".sincerity";

	public static final String IVY_CONF = "ivy.conf";

	public static final String REPOSITORIES_CONF = "repositories.conf";

	public static final String DEPENDENCIES_CONF = "dependencies.conf";

	public static final String ARTIFACTS_CONF = "artifacts.conf";

	public static final String SHORTCUTS_CONF = "shortcuts.conf";

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param sincerity
	 *        The Sincerity instance
	 * @param root
	 *        The container root directory
	 * @param debugLevel
	 *        The Ivy debug level
	 * @throws SincerityException
	 *         In case of an error
	 * @see DefaultMessageLogger
	 */
	public Container( Sincerity sincerity, File root, int debugLevel ) throws SincerityException
	{
		super( root );
		this.sincerity = sincerity;

		root.mkdirs();
		getConfigurationFile( "sincerity" ).mkdirs();

		shortcuts = new Shortcuts( getConfigurationFile( "sincerity", SHORTCUTS_CONF ) );
	}

	/**
	 * The Sincerity instance.
	 * 
	 * @return The Sincerity instance
	 */
	public Sincerity getSincerity()
	{
		return sincerity;
	}

	/**
	 * The specified and resolved dependencies.
	 * 
	 * @return The dependencies
	 */
	public abstract Dependencies<M> getDependencies();

	/**
	 * The specified repositories.
	 * 
	 * @return The repositories
	 */
	public abstract R getRepositories();

	/**
	 * The shortcuts.
	 * 
	 * @return The shortcuts
	 */
	public Shortcuts getShortcuts()
	{
		return shortcuts;
	}

	/**
	 * The cached plugins, based on the current bootstrap.
	 * 
	 * @return The plugins
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Plugins getPlugins() throws SincerityException
	{
		if( plugins == null )
			plugins = new Plugins( this );
		return plugins;
	}

	/**
	 * The cached bootstrap.
	 * 
	 * @return The bootstrap
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Bootstrap getBootstrap() throws SincerityException
	{
		return getBootstrap( false );
	}

	/**
	 * The bootstrap, whether cached or explicitly (re)created.
	 * 
	 * @param forceCreate
	 *        True to ignore cached value and force creation
	 * @return The bootstrap
	 * @throws SincerityException
	 *         In case of an error
	 * @see #createBootstrap()
	 */
	public Bootstrap getBootstrap( boolean forceCreate ) throws SincerityException
	{
		@SuppressWarnings("resource")
		Bootstrap bootstrap = forceCreate ? null : Bootstrap.getBootstrap( getRoot() );
		if( bootstrap == null )
		{
			bootstrap = createBootstrap();
			Bootstrap.setBootstrap( getRoot(), bootstrap );

			// These depend on the bootstrap
			plugins = null;
			languageManager = null;
		}
		return bootstrap;
	}

	/**
	 * The cached Scripturian language manager, based on the current bootstrap.
	 * <p>
	 * Contains explicit support for Python and Velocity.
	 * 
	 * @return The language manager
	 * @throws SincerityException
	 *         In case of an error
	 */
	public LanguageManager getLanguageManager() throws SincerityException
	{
		if( languageManager == null )
		{
			System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH_PROPERTY, getCacheFile().getPath() );

			// The Python standard library is here (Jython expects a "Lib"
			// subdirectory underneath)
			System.setProperty( "python.home", getLibrariesFile( "python" ).getPath() );

			// The cachedir must be absolute or relative to PYTHON_HOME (Jython
			// will add a "packages" subdirectory to it)
			System.setProperty( "python.cachedir", getCacheFile( "python" ).getPath() );

			languageManager = new LanguageManager( getBootstrap() );

			ScripturianUtil.initializeLanguageManager( languageManager );
		}
		return languageManager;
	}

	/**
	 * The cached Scripturian parser manager, based on the current bootstrap.
	 * 
	 * @return The parser manager
	 * @throws SincerityException
	 *         In case of an error
	 */
	public ParserManager getParserManager() throws SincerityException
	{
		if( parserManager == null )
			parserManager = new ParserManager( getBootstrap() );
		return parserManager;
	}

	/**
	 * Creates a packaging context for the container.
	 * 
	 * @return A packaging context
	 * @throws SincerityException
	 *         In case of an error
	 */
	public PackagingContext createPackagingContext() throws SincerityException
	{
		return new PackagingContext( getRoot(), getBootstrap(), sincerity.getOut(), sincerity.getVerbosity() );
	}

	/**
	 * Constructs an absolute path from the Sincerity root directory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 */
	public File getSincerityFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = SINCERITY_DIR;
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	/**
	 * Constructs an absolute path from the "configuration" subdirectory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 */
	public File getConfigurationFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "configuration";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	/**
	 * Constructs an absolute path from the "logs" subdirectory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 */
	public File getLogsFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "logs";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	/**
	 * Constructs an absolute path from the "cache" subdirectory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 */
	public File getCacheFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "cache";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	/**
	 * Constructs an absolute path from the "libraries" subdirectory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 */
	public File getLibrariesFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "libraries";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	/**
	 * Constructs an absolute path from the "programs" subdirectory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 */
	public File getProgramsFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "programs";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	/**
	 * Constructs an absolute path from the "executables" subdirectory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 */
	public File getExecutablesFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "executables";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	/**
	 * The names of all files under the "programs" subdirectory without their
	 * extensions.
	 * 
	 * @return The programs
	 * @throws SincerityException
	 *         In case of an error
	 */
	public List<String> getPrograms() throws SincerityException
	{
		ArrayList<String> programs = new ArrayList<String>();
		File programsDir = getProgramsFile();
		if( programsDir.isDirectory() )
		{
			for( File program : programsDir.listFiles() )
			{
				String name = program.getName();
				int lastDot = name.lastIndexOf( '.' );
				if( lastDot != -1 )
					name = name.substring( 0, lastDot );
				programs.add( name );
			}
		}
		return programs;
	}

	/**
	 * @return True if changed
	 * @see #setChanged(boolean)
	 */
	public boolean hasChanged()
	{
		return hasChanged;
	}

	/**
	 * True if the dependencies have been changed, requiring a new bootstrap.
	 * 
	 * @param hasChanged
	 *        True if changed
	 */
	public void setChanged( boolean hasChanged )
	{
		this.hasChanged = hasChanged;
	}

	/**
	 * @return True if finished installing
	 * @see #setHasFinishedInstalling(boolean)
	 */
	public boolean hasFinishedInstalling()
	{
		return hasFinishedInstalling;
	}

	/**
	 * True if dependencies have finished installing.
	 * 
	 * @param hasFinishedInstalling
	 *        True if finished installing
	 */
	public void setHasFinishedInstalling( boolean hasFinishedInstalling )
	{
		this.hasFinishedInstalling = hasFinishedInstalling;
	}

	/**
	 * @return The number of installations
	 * @see #setInstallations(int)
	 */
	public int getInstallations()
	{
		String installationsName = getInstallationsName();
		Integer installations = (Integer) Bootstrap.getAttributes().get( installationsName );
		if( installations == null )
		{
			installations = 0;
			Bootstrap.getAttributes().put( installationsName, installations );
		}

		return installations;
	}

	/**
	 * The number of installation attempts for this root directory.
	 * <p>
	 * Note that this value will persist even if this class is re-instantiated.
	 * 
	 * @param installations
	 *        The number of installations
	 * @see #addInstallation()
	 */
	public void setInstallations( int installations )
	{
		Bootstrap.getAttributes().put( getInstallationsName(), installations );
	}

	//
	// Operations
	//

	/**
	 * Increase the number of installations by one.
	 * 
	 * @see #setInstallations(int)
	 */
	public void addInstallation()
	{
		setInstallations( getInstallations() + 1 );
	}

	/**
	 * Sets {@link #setChanged(boolean)} to false and
	 * {@link #setHasFinishedInstalling(boolean)} to true.
	 */
	public void initializeProgress()
	{
		setChanged( false );
		setHasFinishedInstalling( true );
	}

	/**
	 * Creates a new bootstrap based on the classpath.
	 * 
	 * @return A bootstrap
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Bootstrap createBootstrap() throws SincerityException
	{
		List<File> classpaths = getDependencies().getClasspaths( false );
		ArrayList<URL> urls = new ArrayList<URL>( classpaths.size() );
		try
		{
			for( File file : classpaths )
				urls.add( file.toURI().toURL() );
		}
		catch( MalformedURLException x )
		{
			throw new SincerityException( "Parsing error while initializing bootstrap", x );
		}

		return new Bootstrap( urls );
	}

	/**
	 * Recreates and caches the bootstrap.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 * @see #getBootstrap(boolean)
	 */
	public void updateBootstrap() throws SincerityException
	{
		getBootstrap( true );
	}

	//
	// TransferListener
	//

	public void transferProgress( TransferEvent event )
	{
		// System.out.println( event );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Sincerity sincerity;

	private final Shortcuts shortcuts;

	private Plugins plugins;

	private LanguageManager languageManager;

	private ParserManager parserManager;

	private boolean hasChanged;

	private boolean hasFinishedInstalling;

	private String getInstallationsName()
	{
		return Container.class.getCanonicalName() + ".installations:" + getRoot();
	}
}

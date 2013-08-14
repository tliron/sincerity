/**
 * Copyright 2011-2013 Three Crickets LLC.
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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.IvyEvent;
import org.apache.ivy.core.event.IvyListener;
import org.apache.ivy.core.event.download.EndArtifactDownloadEvent;
import org.apache.ivy.core.event.download.StartArtifactDownloadEvent;
import org.apache.ivy.core.event.resolve.EndResolveDependencyEvent;
import org.apache.ivy.core.event.resolve.StartResolveDependencyEvent;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.plugins.repository.TransferListener;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.trigger.Trigger;
import org.apache.ivy.util.DefaultMessageLogger;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.ivy.ExtendedResolutionCacheManager;

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
 * @author Tal Liron
 */
public class Container implements IvyListener, TransferListener
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
	 * Creates an Ivy instance using the "ivy.conf" resource.
	 * 
	 * @param sincerity
	 *        The Sincerity instance
	 * @param root
	 *        The container root directory
	 * @param debugLevel
	 *        The Ivy debug level
	 * @throws SincerityException
	 * @see DefaultMessageLogger
	 */
	public Container( Sincerity sincerity, File root, int debugLevel ) throws SincerityException
	{
		this.sincerity = sincerity;
		this.root = root;

		root.mkdirs();
		String rootPath = root.getAbsolutePath();

		// Ivy
		ivy = Ivy.newInstance();
		ivy.getLoggerEngine().pushLogger( new DefaultMessageLogger( debugLevel ) );

		// Listen to events
		ivy.getEventManager().addIvyListener( this );
		ivy.getEventManager().addTransferListener( this );

		// Load settings
		URL settings = Container.class.getResource( IVY_CONF );
		ivy.getSettings().setVariable( "ivy.cache.dir", rootPath );
		ivy.pushContext();
		try
		{
			ivy.getSettings().load( settings );
		}
		catch( ParseException x )
		{
			throw new SincerityException( "Could not parse Ivy settings: " + settings, x );
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not read Ivy settings: " + settings, x );
		}
		finally
		{
			ivy.popContext();
		}

		// Resolution cache manager
		ivy.getSettings().setResolutionCacheManager( new ExtendedResolutionCacheManager( ivy.getSettings().getDefaultResolutionCacheBasedir() ) );

		File configuration = getFile( "configuration", "sincerity" );
		configuration.mkdirs();
		repositories = new Repositories( new File( configuration, REPOSITORIES_CONF ), ivy );
		dependencies = new Dependencies( new File( configuration, DEPENDENCIES_CONF ), new File( configuration, ARTIFACTS_CONF ), this );
		shortcuts = new Shortcuts( new File( configuration, SHORTCUTS_CONF ) );

		configure();
	}

	//
	// Attributes
	//

	/**
	 * The Ivy instance.
	 * 
	 * @return The Ivy instance
	 */
	public Ivy getIvy()
	{
		return ivy;
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
	 * The specified repositories.
	 * 
	 * @return The repositories
	 */
	public Repositories getRepositories()
	{
		return repositories;
	}

	/**
	 * The specified and resolved dependencies.
	 * 
	 * @return The dependencies
	 */
	public Dependencies getDependencies()
	{
		return dependencies;
	}

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
	 * @see #createBootstrap()
	 */
	public Bootstrap getBootstrap( boolean forceCreate ) throws SincerityException
	{
		Bootstrap bootstrap = forceCreate ? null : Bootstrap.getBootstrap( root );
		if( bootstrap == null )
		{
			bootstrap = createBootstrap();
			Bootstrap.setBootstrap( root, bootstrap );

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
	 */
	public LanguageManager getLanguageManager() throws SincerityException
	{
		if( languageManager == null )
		{
			System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, getCacheFile().getPath() );

			// The Python standard library is here (Jython expects a "Lib"
			// subdirectory underneath)
			System.setProperty( "python.home", getLibrariesFile( "python" ).getPath() );

			// The cachedir must be absolute or relative to PYTHON_HOME (Jython
			// will add a "packages" subdirectory to it)
			System.setProperty( "python.cachedir", getCacheFile( "python" ).getPath() );

			languageManager = new LanguageManager( getBootstrap() );

			try
			{
				// Prefer log4j chute for Velocity if log4j exists
				Class.forName( "org.apache.log4j.Logger" );
				languageManager.getAttributes().putIfAbsent( "velocity.runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogChute" );
				languageManager.getAttributes().putIfAbsent( "velocity.runtime.log.logsystem.log4j.logger", "velocity" );
			}
			catch( ClassNotFoundException x )
			{
			}
		}
		return languageManager;
	}

	/**
	 * The absolute path of the root directory.
	 * 
	 * @return The absolute file
	 */
	public File getRoot()
	{
		return root;
	}

	/**
	 * Constructs an absolute path from the root directory.
	 * 
	 * @param parts
	 *        The path parts
	 * @return The absolute file
	 */
	public File getFile( String... parts )
	{
		File file = root;
		for( String part : parts )
			file = new File( file, part );
		return file;
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
	 * Makes sure that we have an absolute file path, using the root directory
	 * as the start if the supplied path is relative.
	 * 
	 * @param file
	 *        The file
	 * @return The absolute file
	 * @see #getRelativeFile(File)
	 */
	public File getAbsoluteFile( File file )
	{
		if( !file.isAbsolute() )
			return new File( root, file.getPath() );
		else
			return file;
	}

	/**
	 * If the file is under to the root directory, returns a relative path.
	 * 
	 * @param file
	 *        The absolute file
	 * @return The relative file
	 * @see #getAbsoluteFile(File)
	 * @see #getRelativePath(String)
	 */
	public File getRelativeFile( File file )
	{
		return new File( getRelativePath( file.getPath() ) );
	}

	/**
	 * If the file is under to the root directory, returns a relative path.
	 * 
	 * @param file
	 *        The absolute file
	 * @return The relative path
	 * @see #getRelativeFile(File)
	 * @see #getRelativePath(File)
	 */
	public String getRelativePath( File file )
	{
		return getRelativePath( file.getAbsolutePath() );
	}

	/**
	 * If the path is under to the root directory, returns a relative path.
	 * 
	 * @param path
	 *        The absolute path
	 * @return The relative path
	 * @see #getRelativePath(File)
	 */
	public String getRelativePath( String path )
	{
		String root = this.root.getPath();
		if( path.startsWith( root ) )
			path = path.substring( root.length() + 1 );
		return path;
	}

	/**
	 * The names of all files under the "programs" subdirectory without their
	 * extensions.
	 * 
	 * @return The programs
	 * @throws SincerityException
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
		hasChanged = false;
		hasFinishedInstalling = true;
	}

	/**
	 * Creates a new bootstrap based on the classpath.
	 * 
	 * @return A bootstrap
	 * @throws SincerityException
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
	 * @see {@link #getBootstrap(boolean)}
	 */
	public void updateBootstrap() throws SincerityException
	{
		getBootstrap( true );
	}

	//
	// IvyListener
	//

	public void progress( IvyEvent event )
	{
		String name = event.getName();
		Map<?, ?> attributes = event.getAttributes();
		if( StartArtifactDownloadEvent.NAME.equals( name ) )
		{
			// for (Object o : attributes.keySet())
			// System.out.println(o + ": " + attributes.get(o));
			if( "false".equals( attributes.get( "metadata" ) ) )
			{
				String origin = (String) attributes.get( "origin" );
				if( sincerity.getVerbosity() >= 1 )
					sincerity.getOut().println( "Downloading from: " + origin );
			}
		}
		else if( EndArtifactDownloadEvent.NAME.equals( name ) )
		{
			if( "false".equals( attributes.get( "metadata" ) ) && "successful".equals( attributes.get( "status" ) ) )
			{
				String file = (String) attributes.get( "file" );
				if( sincerity.getVerbosity() >= 1 )
					sincerity.getOut().println( "Installing artifact: " + getRelativePath( file ) );
				hasChanged = true;
			}
		}
		else if( StartResolveDependencyEvent.NAME.equals( name ) )
		{
			String organization = (String) attributes.get( "organisation" );
			String module = (String) attributes.get( "module" );
			String revision = (String) attributes.get( "revision" );
			if( "latest.integration".equals( revision ) )
				revision = "";
			else
				revision = ", " + revision;
			if( sincerity.getVerbosity() >= 1 )
				sincerity.getOut().println( "Checking: " + organization + ":" + module + revision );
		}
		else if( EndResolveDependencyEvent.NAME.equals( name ) )
		{
			String resolved = (String) attributes.get( "resolved" );
			if( "false".equals( resolved ) )
			{
				String organization = (String) attributes.get( "organisation" );
				String module = (String) attributes.get( "module" );
				String artifact = (String) attributes.get( "artifact" );
				String revision = (String) attributes.get( "revision" );
				sincerity.getOut().println( "Could not find in repositories:\n  package: " + organization + ":" + module + ":" + artifact + ":" + revision );
			}
		}
		else
		{
			// System.out.println( event );
		}
	}

	//
	// TransferListener
	//

	public void transferProgress( TransferEvent event )
	{
		// System.out.println( event );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Sincerity sincerity;

	private final File root;

	private final Ivy ivy;

	private final Repositories repositories;

	private final Dependencies dependencies;

	private final Shortcuts shortcuts;

	private Plugins plugins;

	private LanguageManager languageManager;

	private boolean hasChanged;

	private boolean hasFinishedInstalling;

	private void configure()
	{
		// A version of this exists privately in
		// org.apache.ivy.Ivy#postConfigure()
		for( Object t : ivy.getSettings().getTriggers() )
		{
			Trigger trigger = (Trigger) t;
			ivy.getEventManager().addIvyListener( trigger, trigger.getEventFilter() );
		}

		for( Object r : ivy.getSettings().getResolvers() )
		{
			DependencyResolver resolver = (DependencyResolver) r;
			if( resolver instanceof BasicResolver )
				( (BasicResolver) resolver ).setEventManager( ivy.getEventManager() );
		}
	}

	private String getInstallationsName()
	{
		return Container.class.getCanonicalName() + ".installations:" + root;
	}
}

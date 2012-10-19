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

	public Ivy getIvy()
	{
		return ivy;
	}

	public Sincerity getSincerity()
	{
		return sincerity;
	}

	public Repositories getRepositories()
	{
		return repositories;
	}

	public Dependencies getDependencies()
	{
		return dependencies;
	}

	public Shortcuts getShortcuts()
	{
		return shortcuts;
	}

	public Plugins getPlugins() throws SincerityException
	{
		if( plugins == null )
			plugins = new Plugins( this );
		return plugins;
	}

	public Bootstrap getBootstrap() throws SincerityException
	{
		return getBootstrap( false );
	}

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

	public void updateBootstrap() throws SincerityException
	{
		getBootstrap( true );
		/*
		 * for( File file : getClasspaths( false ) ) bootstrap.addFile( file );
		 */
	}

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

	public File getRoot()
	{
		return root;
	}

	public File getFile( String... parts )
	{
		File file = root;
		for( String part : parts )
			file = new File( file, part );
		return file;
	}

	public File getConfigurationFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "configuration";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	public File getLogsFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "logs";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	public File getCacheFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "cache";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	public File getLibrariesFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "libraries";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	public File getProgramsFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "programs";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	public File getExecutablesFile( String... parts )
	{
		String[] newParts = new String[parts.length + 1];
		newParts[0] = "executables";
		System.arraycopy( parts, 0, newParts, 1, parts.length );
		return getFile( newParts );
	}

	public File getAbsoluteFile( File file )
	{
		if( !file.isAbsolute() )
			return new File( root, file.getPath() );
		else
			return file;
	}

	public File getRelativeFile( File file )
	{
		return new File( getRelativePath( file.getPath() ) );
	}

	public String getRelativePath( File file )
	{
		return getRelativePath( file.getAbsolutePath() );
	}

	public String getRelativePath( String path )
	{
		String root = this.root.getPath();
		if( path.startsWith( root ) )
			path = path.substring( root.length() + 1 );
		return path;
	}

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

	public boolean hasChanged()
	{
		return hasChanged;
	}

	public void setChanged( boolean hasChanged )
	{
		this.hasChanged = hasChanged;
	}

	public boolean hasInstalled()
	{
		return hasInstalled;
	}

	public void setInstalled( boolean hasSecondResolvePhase )
	{
		this.hasInstalled = hasSecondResolvePhase;
	}

	public void initializeProgress()
	{
		hasChanged = false;
		hasInstalled = true;
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

	private boolean hasInstalled;

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
}

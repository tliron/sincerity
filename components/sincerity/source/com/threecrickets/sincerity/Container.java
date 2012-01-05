package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.Map;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.event.IvyEvent;
import org.apache.ivy.core.event.IvyListener;
import org.apache.ivy.core.event.download.EndArtifactDownloadEvent;
import org.apache.ivy.core.event.download.StartArtifactDownloadEvent;
import org.apache.ivy.core.event.resolve.EndResolveDependencyEvent;
import org.apache.ivy.plugins.repository.TransferEvent;
import org.apache.ivy.plugins.repository.TransferListener;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.trigger.Trigger;
import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message;

import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.ivy.ExtendedResolutionCacheManager;

/**
 * Sincerity is a full-featured, straightforward package manager for the JVM. It
 * supports many JVM languages and repository technologies.
 * <p>
 * Sincerity differs from other package managers in that it manages and installs
 * packages into a self-contained container, allowing for isolated, deployable
 * environments. It can be used to very simply distribute a single application
 * with or without its dependencies. Whatever is missing will be automatically
 * downloaded from your managed repository, or from public repositories should
 * you allow it. Application or dependency upgrades can be easily handled
 * programatically, manually or automatically.
 * <p>
 * But Sincerity can handle more complex containers, which in turn run other
 * services internally. For example, RESTful web containers based on <a
 * href="http://threecrickets.com/prudence/">Prudence</a>, or OSGi containers
 * based on <a href="https://felix.apache.org/">Felix</a>.
 * <p>
 * Essential packages take care of centralizing your logging, installing your
 * application as an operating system daemon or service, and allowing for remote
 * management via JMX.
 * <p>
 * Working with Sincerity is easy, and even fun: its usage paradigm is
 * "by command" rather than "by configuration," meaning that you will never have
 * to edit monstrous XML files. Instead, you tell Sincerity what you want to do,
 * for example: "add this package (and its dependencies)," "remove this package"
 * or "package this code and deploy it to a repository." It comes with a command
 * line interface and a Swing-based GUI. Additional packages let you install
 * Prudence-based web frontend. The API, written in Java with non-Java languages
 * in mind, is well-documented and easy to embed.
 * <p>
 * The usual package management features are supported:
 * <p>
 * <ul>
 * <li>Intelligent dependency resolution based on versions</li>
 * <li>Packaging and deployment to repositories</li>
 * </ul>
 * <p>
 * The following repository technologies are supported:
 * <ul>
 * <li>Java, Clojure, Scala and other JVM libraries from Maven and Ivy ibiblio
 * repositories</li>
 * <li>Python eggs from PyPI or compatible repositories (similarly to
 * easy_install and PIP tools)</li>
 * <li>Ruby gems</li>
 * <li>PHP packages from PEAR or compatible repositories (similarly to the pecl
 * tool)</li>
 * </ul>
 * Under the hood, Sincerity relies on <a
 * href="https://ant.apache.org/ivy/">Ivy</a> and <a
 * href="http://threecrickets.com/scripturian/">Scripturian</a>, which are both
 * lightweight and minimal. Plugins for each are implicitly supported.
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

	public Container( Sincerity sincerity ) throws SincerityException
	{
		this( sincerity, null, Message.MSG_WARN );
	}

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

	public Bootstrap getClassLoader() throws SincerityException
	{
		return Bootstrap.getBootstrap( root );
	}

	public LanguageManager getLanguageManager() throws SincerityException
	{
		if( languageManager == null )
		{
			System.setProperty( LanguageManager.SCRIPTURIAN_CACHE_PATH, getCacheFile().getPath() );
			languageManager = new LanguageManager( getClassLoader() );
			languageManager.getAttributes().put( "velocity.runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogChute" );
			languageManager.getAttributes().put( "velocity.runtime.log.logsystem.log4j.logger", "velocity" );
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

	//
	// IvyListener
	//

	public void progress( IvyEvent event )
	{
		if( sincerity.getVerbosity() < 1 )
			return;

		String name = event.getName();
		Map<?, ?> attributes = event.getAttributes();
		if( StartArtifactDownloadEvent.NAME.equals( name ) )
		{
			// for (Object o : attributes.keySet())
			// System.out.println(o + ": " + attributes.get(o));
			if( "false".equals( attributes.get( "metadata" ) ) )
			{
				String origin = (String) attributes.get( "origin" );
				sincerity.getOut().println( "Downloading " + origin );
			}
		}
		else if( EndArtifactDownloadEvent.NAME.equals( name ) )
		{
			if( "false".equals( attributes.get( "metadata" ) ) && "successful".equals( attributes.get( "status" ) ) )
			{
				String file = (String) attributes.get( "file" );
				sincerity.getOut().println( "Installing artifact: " + getRelativePath( file ) );
			}
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

	private LanguageManager languageManager;

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

package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

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

	public static final String SINCERITY_DIR_NAME = ".sincerity";

	//
	// Construction
	//

	public Container() throws ParseException, IOException
	{
		this( null, Message.MSG_WARN );
	}

	public Container( File root, int debugLevel ) throws ParseException, IOException
	{
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
		ivy.getSettings().setVariable( "ivy.cache.dir", rootPath );
		ivy.pushContext();
		ivy.getSettings().load( Container.class.getResource( "ivy.conf" ) );
		ivy.popContext();

		// Resolution cache manager
		ivy.getSettings().setResolutionCacheManager( new ExtendedResolutionCacheManager( ivy.getSettings().getDefaultResolutionCacheBasedir() ) );

		File configuration = new File( root, "configuration/sincerity" );
		configuration.mkdirs();
		repositories = new Repositories( new File( configuration, "repositories.conf" ), ivy );
		dependencies = new Dependencies( new File( configuration, "dependencies.conf" ), new File( configuration, "artifacts.conf" ), this );
		aliases = new Aliases( new File( configuration, "aliases.conf" ) );

		configure();
	}

	//
	// Attributes
	//

	public File getRoot()
	{
		return root;
	}

	public Ivy getIvy()
	{
		return ivy;
	}

	public Repositories getRepositories()
	{
		return repositories;
	}

	public Dependencies getDependencies()
	{
		return dependencies;
	}

	public Aliases getAliases()
	{
		return aliases;
	}

	public String getRelativePath( File file )
	{
		return getRelativePath( file.getPath() );
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
		String name = event.getName();
		Map<?, ?> attributes = event.getAttributes();
		if( StartArtifactDownloadEvent.NAME.equals( name ) )
		{
			String organization = (String) attributes.get( "organisation" );
			String module = (String) attributes.get( "module" );
			String artifact = (String) attributes.get( "artifact" );
			String revision = (String) attributes.get( "revision" );
			String type = (String) attributes.get( "type" );
			String origin = (String) attributes.get( "origin" );
			if( name == null )
				message( "Fetching file:\n  type: " + type + "\n  package: " + organization + ":" + module + ":" + artifact + ":" + revision + "\n  from: " + origin );
			origins.add( origin );
		}
		else if( EndArtifactDownloadEvent.NAME.equals( name ) )
		{
			String origin = (String) attributes.get( "origin" );
			if( origins.remove( origin ) )
			{
				String file = (String) attributes.get( "file" );
				message( "Installed artifact: " + getRelativePath( file ) );
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
				message( "Could not find in repositories:\n  package: " + organization + ":" + module + ":" + artifact + ":" + revision );
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

	private final File root;

	private final Ivy ivy;

	private final Repositories repositories;

	private final Dependencies dependencies;

	private final Aliases aliases;

	private final List<String> origins = new CopyOnWriteArrayList<String>();

	private void message( String message )
	{
		System.out.println( message );
	}

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

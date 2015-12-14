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

package com.threecrickets.sincerity.ivy;

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
import org.apache.ivy.core.event.resolve.StartResolveDependencyEvent;
import org.apache.ivy.plugins.repository.TransferListener;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.trigger.Trigger;
import org.apache.ivy.util.DefaultMessageLogger;

import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;

public class IvyContainer extends Container<IvyResolvedDependency, IvyRepositories> implements IvyListener, TransferListener
{
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
	 *         In case of an error
	 * @see DefaultMessageLogger
	 */
	public IvyContainer( Sincerity sincerity, File root, int debugLevel ) throws SincerityException
	{
		super( sincerity, root, debugLevel );

		// Ivy
		ivy = Ivy.newInstance();
		ivy.getLoggerEngine().pushLogger( new DefaultMessageLogger( debugLevel ) );

		// Listen to events
		ivy.getEventManager().addIvyListener( this );
		ivy.getEventManager().addTransferListener( this );

		// Load settings
		URL settings = Container.class.getResource( IVY_CONF );
		ivy.getSettings().setVariable( "ivy.cache.dir", root.getAbsolutePath() );
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
		ivy.getSettings().setResolutionCacheManager( new ExtendedResolutionCacheManager( new File( ivy.getSettings().getDefaultResolutionCacheBasedir(), "cache/sincerity" ), getSincerityFile( "resolution" ) ) );

		configure();

		repositories = new IvyRepositories( getConfigurationFile( "sincerity", REPOSITORIES_CONF ), ivy );
		dependencies = new IvyDependencies( getConfigurationFile( "sincerity", DEPENDENCIES_CONF ), getConfigurationFile( "sincerity", ARTIFACTS_CONF ), this );
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

	//
	// Container
	//

	@Override
	public IvyRepositories getRepositories()
	{
		return repositories;
	}

	@Override
	public Dependencies<IvyResolvedDependency> getDependencies()
	{
		return dependencies;
	}

	//
	// IvyListener
	//

	public void progress( IvyEvent event )
	{
		Sincerity sincerity = getSincerity();
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
				setChanged( true );
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Ivy ivy;

	private final IvyRepositories repositories;

	private final IvyDependencies dependencies;

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

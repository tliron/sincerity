/**
 * Copyright 2011-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.dependencies.creel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.threecrickets.creel.Configuration;
import com.threecrickets.creel.Engine;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.event.JLineEventHandler;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * @author Tal Liron
 */
public class CreelContainer extends Container<CreelModule, CreelRepositories>
{
	//
	// Construction
	//

	public CreelContainer( Sincerity sincerity, File root, int debugLevel ) throws SincerityException
	{
		super( sincerity, root, debugLevel );

		engine = new Engine();

		( (EventHandlers) engine.getEventHandler() ).add( new JLineEventHandler( sincerity.isTerminalAnsi(), sincerity.getVerbosity() > 1 ) );

		try
		{
			engine.getDirectories().setDefault( getFile() );
			engine.getDirectories().setLibrary( getLibrariesFile( "jars" ) );
			engine.getDirectories().setApi( getFile( "reference", "api" ) );
			engine.getDirectories().setSource( getFile( "reference", "source" ) );
			engine.setStateFile( getConfigurationFile( "sincerity", "creel.state" ) );
		}
		catch( IOException x )
		{
			throw new SincerityException( x );
		}

		configurationFile = getConfigurationFile( "sincerity", "creel.properties" );

		loadConfiguration();

		dependencies = new CreelDependencies( null, this );
		repositories = new CreelRepositories( this );
	}

	//
	// Operations
	//

	public void loadConfiguration() throws SincerityException
	{
		try
		{
			Configuration configuration = new Configuration( configurationFile );
			engine.setModules( configuration.getModuleSpecificationConfigs() );
			engine.setRepositories( configuration.getRepositoryConfigs() );
			engine.setRules( configuration.getRuleConfigs() );
		}
		catch( FileNotFoundException x )
		{
			return;
		}
		catch( IOException x )
		{
			throw new SincerityException( x );
		}
	}

	//
	// Container
	//

	@Override
	public Dependencies<CreelModule> getDependencies()
	{
		return dependencies;
	}

	@Override
	public CreelRepositories getRepositories()
	{
		return repositories;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected final Engine engine;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File configurationFile;

	private final CreelDependencies dependencies;

	private final CreelRepositories repositories;
}

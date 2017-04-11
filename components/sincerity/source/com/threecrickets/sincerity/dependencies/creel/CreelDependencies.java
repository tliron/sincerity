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
import java.util.List;
import java.util.Set;

import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.dependencies.Modules;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.packaging.Artifact;

/**
 * @author Tal Liron
 */
public class CreelDependencies extends Dependencies<CreelModule>
{
	//
	// Dependencies
	//

	@Override
	public boolean hasExplicitDependency( String group, String name, String version )
	{
		return false;
	}

	@Override
	public Modules<CreelModule> getModules() throws SincerityException
	{
		if( modules == null )
			modules = new CreelModules( (CreelContainer) getContainer() );
		return modules;
	}

	@Override
	public Set<Artifact> getArtifacts( boolean install, boolean overwrite, boolean verify ) throws SincerityException
	{
		return null;
	}

	@Override
	public List<File> getClasspaths( boolean includeSystem ) throws SincerityException
	{
		List<File> classpaths = super.getClasspaths( includeSystem );

		// TODO: add jars from our artifacts

		return classpaths;
	}

	@Override
	public void reset() throws SincerityException
	{
	}

	@Override
	public boolean addExplicitDependency( String group, String name, String version, boolean force, boolean transitive ) throws SincerityException
	{
		return false;
	}

	@Override
	public boolean reviseExplicitDependency( String group, String name, String newVersion ) throws SincerityException
	{
		return false;
	}

	@Override
	public boolean removeExplicitDependency( String group, String name ) throws SincerityException
	{
		return false;
	}

	@Override
	public boolean excludeDependency( String group, String name ) throws SincerityException
	{
		return false;
	}

	@Override
	public boolean overrideDependency( String group, String name, String version ) throws SincerityException
	{
		return false;
	}

	@Override
	public void freezeVersions() throws SincerityException
	{
	}

	@Override
	public void install( boolean overwrite, boolean verify ) throws SincerityException
	{
		CreelContainer container = (CreelContainer) getContainer();
		int installations = container.getInstallations();
		if( installations == 0 )
			container.getSincerity().getOut().println( "Making sure all dependencies are installed and upgraded..." );

		container.initializeProgress();

		container.engine.run();

		container.updateBootstrap();

		container.addInstallation();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected CreelDependencies( File artifactsFile, CreelContainer container ) throws SincerityException
	{
		super( artifactsFile, container );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private CreelModules modules;
}

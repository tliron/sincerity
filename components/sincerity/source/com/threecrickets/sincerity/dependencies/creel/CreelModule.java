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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.threecrickets.creel.exception.IncompatiblePlatformException;
import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.sincerity.dependencies.Artifact;
import com.threecrickets.sincerity.dependencies.License;
import com.threecrickets.sincerity.dependencies.Module;

/**
 * A wrapper around a Creel {@link com.threecrickets.creel.Module}.
 * 
 * @author Tal Liron
 */
public class CreelModule extends Module
{
	//
	// Module
	//

	@Override
	public String getGroup()
	{
		try
		{
			MavenModuleIdentifier moduleIdentifier = MavenModuleIdentifier.cast( module.getIdentifier() );
			return moduleIdentifier.getGroup();
		}
		catch( IncompatiblePlatformException x )
		{
			return null;
		}
	}

	@Override
	public String getName()
	{
		try
		{
			MavenModuleIdentifier moduleIdentifier = MavenModuleIdentifier.cast( module.getIdentifier() );
			return moduleIdentifier.getName();
		}
		catch( IncompatiblePlatformException x )
		{
			return null;
		}
	}

	@Override
	public String getVersion()
	{
		try
		{
			MavenModuleIdentifier moduleIdentifier = MavenModuleIdentifier.cast( module.getIdentifier() );
			return moduleIdentifier.getVersion();
		}
		catch( IncompatiblePlatformException x )
		{
			return null;
		}
	}

	@Override
	public Collection<Module> getChildren()
	{
		Collection<Module> children = new ArrayList<Module>();
		for( com.threecrickets.creel.Module dependency : module.getDependencies() )
			children.add( new CreelModule( dependency, container ) );
		return children;
	}

	@Override
	public Collection<License> getLicenses()
	{
		return Collections.emptyList();
	}

	@Override
	public Collection<Artifact> getArtifacts()
	{
		return null;
	}

	@Override
	public boolean isExplicitDependency()
	{
		return module.isExplicit();
	}

	@Override
	public boolean isEvicted()
	{
		return false;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		if( module.getIdentifier() != null )
			return module.getIdentifier().toString();
		else
			return module.getSpecification().toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected CreelModule( com.threecrickets.creel.Module module, CreelContainer container )
	{
		this.module = module;
		this.container = container;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final com.threecrickets.creel.Module module;

	private CreelContainer container;
}

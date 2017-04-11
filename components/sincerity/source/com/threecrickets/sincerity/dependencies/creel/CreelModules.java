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
import java.util.List;

import com.threecrickets.sincerity.dependencies.Artifact;
import com.threecrickets.sincerity.dependencies.License;
import com.threecrickets.sincerity.dependencies.Modules;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * @author Tal Liron
 */
public class CreelModules extends Modules<CreelModule>
{
	//
	// Modules
	//

	@Override
	public Collection<CreelModule> getAll()
	{
		return null;
	}

	@Override
	public Collection<Artifact> getArtifacts()
	{
		return null;
	}

	@Override
	public Collection<License> getLicenses()
	{
		return null;
	}

	@Override
	public Collection<CreelModule> getByLicense( License license )
	{
		return null;
	}

	@Override
	public String getVersion( String group, String name ) throws SincerityException
	{
		return null;
	}

	//
	// AbstractList
	//

	@Override
	public int size()
	{
		return roots.size();
	}

	@Override
	public CreelModule get( int index )
	{
		return roots.get( index );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected CreelModules( CreelContainer container )
	{
		this.container = container;

		// State state = new State( container.engine.getStateFile(),
		// container.engine.getDirectories() );

		for( com.threecrickets.creel.Module module : container.engine.getModules() )
			roots.add( new CreelModule( module, container ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private CreelContainer container;

	private final List<CreelModule> roots = new ArrayList<CreelModule>();
}

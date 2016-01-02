/**
 * Copyright 2011-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.dependencies.ivy;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import com.threecrickets.sincerity.dependencies.Artifact;
import com.threecrickets.sincerity.dependencies.License;
import com.threecrickets.sincerity.dependencies.Module;

/**
 * A wrapper around an Ivy {@link ModuleDescriptor}.
 * 
 * @author Tal Liron
 */
public class IvyModule extends Module
{
	//
	// Module
	//

	@Override
	public String getGroup()
	{
		return descriptor.getModuleRevisionId().getOrganisation();
	}

	@Override
	public String getName()
	{
		return descriptor.getModuleRevisionId().getName();
	}

	@Override
	public String getVersion()
	{
		return descriptor.getModuleRevisionId().getRevision();
	}

	@Override
	public Collection<Module> getChildren()
	{
		return children;
	}

	@Override
	public Collection<License> getLicenses()
	{
		org.apache.ivy.core.module.descriptor.License ivyLicenses[] = descriptor.getLicenses();
		ArrayList<License> licenses = new ArrayList<License>( ivyLicenses.length );
		for( org.apache.ivy.core.module.descriptor.License ivyLicense : ivyLicenses )
			licenses.add( new IvyLicense( ivyLicense ) );
		return licenses;
	}

	@Override
	public Collection<Artifact> getArtifacts()
	{
		org.apache.ivy.core.module.descriptor.Artifact ivyArtifacts[] = descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION );
		ArrayList<Artifact> artifacts = new ArrayList<Artifact>( ivyArtifacts.length );
		for( org.apache.ivy.core.module.descriptor.Artifact ivyArtifact : ivyArtifacts )
			artifacts.add( new IvyArtifact( ivyArtifact ) );
		return artifacts;
	}

	@Override
	public boolean isExplicitDependency()
	{
		return isExplicitDependency;
	}

	@Override
	public boolean isEvicted()
	{
		return isEvicted;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		ModuleRevisionId id = descriptor.getModuleRevisionId();
		String organisation = id.getOrganisation();
		String name = id.getName();
		String revision = id.getRevision();
		StringBuilder r = new StringBuilder();
		if( isEvicted )
			r.append( '(' );
		r.append( organisation );
		if( !name.equals( organisation ) )
		{
			r.append( ':' );
			r.append( name );
		}
		if( !"latest.integration".equals( revision ) )
		{
			r.append( " v" );
			r.append( revision );
		}
		if( isEvicted )
			r.append( ')' );
		return r.toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Constructor.
	 * 
	 * @param descriptor
	 *        The Ivy module descriptor
	 * @param evicted
	 *        The Ivy eviction information
	 */
	protected IvyModule( ModuleDescriptor descriptor, String evicted )
	{
		this.descriptor = descriptor;
		this.isEvicted = ( evicted != null ) && !evicted.isEmpty();
	}

	protected void setEplicitDependency( boolean isExplicitDependency )
	{
		this.isExplicitDependency = isExplicitDependency;
	}

	/**
	 * The callers.
	 */
	protected final ArrayList<Caller> callers = new ArrayList<Caller>();

	/**
	 * Caller information record.
	 */
	protected static class Caller
	{
		public Caller( String organisation, String name, String revision )
		{
			this.organisation = organisation;
			this.name = name;
			this.revision = revision;
		}

		public final String organisation;

		public final String name;

		public final String revision;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ModuleDescriptor descriptor;

	private final ArrayList<Module> children = new ArrayList<Module>();

	private final boolean isEvicted;

	private boolean isExplicitDependency = true;
}

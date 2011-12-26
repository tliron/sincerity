package com.threecrickets.sincerity;

import java.util.ArrayList;

import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import com.threecrickets.sincerity.ResolvedDependencies.Caller;

public class ResolvedDependency
{
	//
	// Attributes
	//

	public final ModuleDescriptor descriptor;

	public final String evicted;

	public final ArrayList<ResolvedDependency> children = new ArrayList<ResolvedDependency>();

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
		if( evicted != null )
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
		if( evicted != null )
			r.append( ')' );
		return r.toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected ResolvedDependency( ModuleDescriptor descriptor, String evicted )
	{
		this.descriptor = descriptor;
		this.evicted = ( evicted != null && evicted.length() == 0 ) ? null : evicted;
	}

	protected boolean isRoot = true;

	protected final ArrayList<Caller> callers = new ArrayList<Caller>();
}
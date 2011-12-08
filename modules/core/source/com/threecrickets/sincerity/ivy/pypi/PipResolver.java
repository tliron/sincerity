package com.threecrickets.sincerity.ivy.pypi;

import java.io.File;
import java.util.Collections;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.search.OrganisationEntry;
import org.apache.ivy.plugins.resolver.URLResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.plugins.resolver.util.ResourceMDParser;

public class PipResolver extends URLResolver
{
	//
	// Constants
	//

	public static final String DEFAULT_ORGANIZATION = "python";

	public static final String DEFAULT_PATTERN = "[module]/[artifact]-[revision].[ext]";

	//
	// Construction
	//

	public PipResolver()
	{
		super();
	}

	//
	// Attributes
	//

	public String getOrganisation()
	{
		return organisation;
	}

	public void setOrganisation( String organisation )
	{
		if( organisation == null )
			throw new NullPointerException( "organisation must not be null" );
		this.organisation = organisation;
	}

	public String getRoot()
	{
		return root;
	}

	public void setRoot( String root )
	{
		if( root == null )
			throw new NullPointerException( "root must not be null" );
		if( !root.endsWith( "/" ) )
			this.root = root + "/";
		else
			this.root = root;
		setIvyPatterns( Collections.singletonList( this.root + DEFAULT_PATTERN ) );
	}

	//
	// DependencyResolver
	//

	@Override
	public String getTypeName()
	{
		return "pip";
	}

	@Override
	public ResolvedResource findIvyFileRef( DependencyDescriptor descriptor, ResolveData data )
	{
		ModuleRevisionId id = descriptor.getDependencyRevisionId();
		if( !getOrganisation().equals( id.getOrganisation() ) )
			return null;

		try
		{
			if( pip == null )
				pip = new Python( "piprun.py", new File( "container/libraries/python" ) );

			File log = new File( "container/logs/pip.log" );
			File cache = new File( "container/cache/sincerity/pip/downloads" );
			File build = new File( "container/cache/sincerity/pip/build" );
			File scripts = new File( "container/run" );
			File libraries = new File( "container/libraries/python" );
			File src = new File( "container/reference/source" );

			log.getParentFile().mkdirs();
			cache.mkdirs();
			build.mkdirs();
			src.mkdirs();

			pip.call( "run", "install", "--verbose", "--index-url=" + getRoot(), "--log=" + log.getAbsolutePath(), "--download-cache=" + cache.getAbsolutePath(), "--src=" + src.getAbsolutePath(),
				"--build=" + build.getAbsolutePath(), "--install-option=--install-purelib=" + libraries.getAbsolutePath(), "--install-option=--install-scripts=" + scripts.getAbsolutePath(),
				id.getName() + "==" + id.getRevision() );

			return new ResolvedResource( null, id.getRevision() );
		}
		catch( Exception x )
		{
			// x.printStackTrace();
		}

		return null;
	}

	public ResolvedResource findIvyFileRef3( DependencyDescriptor descriptor, ResolveData data )
	{
		System.out.println( "pypi looking..." );
		ModuleRevisionId id = descriptor.getDependencyRevisionId();
		Artifact artifact = new DefaultArtifact( id, data.getDate(), id.getName(), "tarball", "tar.gz", false );
		System.out.println( artifact );
		ResourceMDParser parser = getRMDParser( descriptor, data );
		ResolvedResource resolved = findResourceUsingPatterns( id, getIvyPatterns(), artifact, parser, data.getDate() );
		System.out.println( resolved );
		return resolved;
	}

	@Override
	public OrganisationEntry[] listOrganisations()
	{
		return new OrganisationEntry[]
		{
			new OrganisationEntry( this, getOrganisation() )
		};
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private String organisation = DEFAULT_ORGANIZATION;

	private String root;

	private static Python pip;
}

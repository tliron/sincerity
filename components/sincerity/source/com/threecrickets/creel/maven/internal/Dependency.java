package com.threecrickets.creel.maven.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.w3c.dom.Element;

import com.threecrickets.sincerity.util.XmlUtil;

public class Dependency
{
	//
	// Construction
	//

	public Dependency( Element element, Properties properties )
	{
		groupId = properties.interpolate( XmlUtil.getFirstElementText( element, "groupId" ) );
		artifactId = properties.interpolate( XmlUtil.getFirstElementText( element, "artifactId" ) );
		version = properties.interpolate( XmlUtil.getFirstElementText( element, "version" ) );
		type = properties.interpolate( XmlUtil.getFirstElementText( element, "type" ) );
		scope = properties.interpolate( XmlUtil.getFirstElementText( element, "scope" ) );
		optional = "true".equals( properties.interpolate( XmlUtil.getFirstElementText( element, "optional" ) ) );

		// <exclusions>, <excluision>
		for( Element exclusion : new XmlUtil.Elements( XmlUtil.getFirstElement( element, "exclusions" ), "exclusion" ) )
			this.exclusions.add( new Exclusion( exclusion, properties ) );
	}

	//
	// Attributes
	//

	public String getGroupId()
	{
		return groupId;
	}

	public String getArtifactId()
	{
		return artifactId;
	}

	public String getVersion()
	{
		return version;
	}

	public String getType()
	{
		return type;
	}

	public String getScope()
	{
		return scope;
	}

	public boolean isOptional()
	{
		return optional;
	}

	public Iterable<Exclusion> getExclusions()
	{
		return Collections.unmodifiableCollection( exclusions );
	}

	public boolean isOmitted()
	{
		return optional || "provided".equals( scope ) || "system".equals( scope ) || "test".equals( scope );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String groupId;

	private final String artifactId;

	private final String version;

	private final String type;

	private final String scope;

	private final boolean optional;

	private final Collection<Exclusion> exclusions = new ArrayList<Exclusion>();
}
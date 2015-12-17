package com.threecrickets.creel.maven.internal;

import org.w3c.dom.Element;

import com.threecrickets.sincerity.util.XmlUtil;

public class Exclusion
{
	//
	// Construction
	//

	public Exclusion( Element element )
	{
		groupId = XmlUtil.getFirstElementText( element, "groupId" );
		artifactId = XmlUtil.getFirstElementText( element, "artifactId" );
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String groupId;

	private final String artifactId;
}
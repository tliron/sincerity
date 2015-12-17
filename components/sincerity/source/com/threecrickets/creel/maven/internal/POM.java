package com.threecrickets.creel.maven.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.creel.maven.MavenModuleSpecification;
import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.sincerity.util.IoUtil;
import com.threecrickets.sincerity.util.XmlUtil;

public class POM
{
	//
	// Construction
	//

	public POM( URL url ) throws IOException
	{
		Document document;
		try
		{
			document = XmlUtil.parse( IoUtil.readText( url ) );
		}
		catch( ParserConfigurationException x )
		{
			throw new RuntimeException( x );
		}
		catch( SAXException x )
		{
			throw new RuntimeException( "Invalid POM", x );
		}

		// <project>
		Element project = XmlUtil.getFirstElement( document.getDocumentElement(), "project" );
		if( project == null )
			throw new RuntimeException( "Invalid POM: no <project>" );

		// <properties>
		Element properties = XmlUtil.getFirstElement( project, "properties" );
		if( properties != null )
		{
			for( Element property : new XmlUtil.Elements( properties ) )
			{
				String name = property.getTagName();
				String value = property.getTextContent();
				this.properties.put( name, value );
			}
		}

		// <parent>
		Element parent = XmlUtil.getFirstElement( project, "parent" );
		if( parent != null )
		{
			parentGroupId = XmlUtil.getFirstElementText( parent, "groupId" );
			parentVersion = XmlUtil.getFirstElementText( parent, "version" );
		}
		else
		{
			parentGroupId = null;
			parentVersion = null;
		}

		groupId = XmlUtil.getFirstElementText( project, "groupId" );
		artifactId = XmlUtil.getFirstElementText( project, "artifactId" );
		version = XmlUtil.getFirstElementText( project, "version" );
		name = XmlUtil.getFirstElementText( project, "name" );
		description = XmlUtil.getFirstElementText( project, "description" );

		// <dependencies>
		Element dependencies = XmlUtil.getFirstElement( project, "dependencies" );
		if( dependencies != null )
			// <dependency>
			for( Element dependency : new XmlUtil.Elements( properties, "dependency" ) )
				this.dependencies.add( new Dependency( dependency ) );
	}

	//
	// Attributes
	//

	public Map<String, String> getProperties()
	{
		return Collections.unmodifiableMap( properties );
	}

	public String getParentGroupId()
	{
		return parentGroupId;
	}

	public String getParentVersion()
	{
		return parentVersion;
	}

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

	public String getName()
	{
		return name;
	}

	public String getDescription()
	{
		return description;
	}

	public Iterable<Dependency> getDependencies()
	{
		return Collections.unmodifiableCollection( dependencies );
	}

	public MavenModuleIdentifier getModuleIdentifier( MavenRepository repository )
	{
		return new MavenModuleIdentifier( repository, groupId != null ? groupId : parentGroupId, artifactId, version != null ? version : parentVersion );
	}

	public Iterable<MavenModuleSpecification> getDependencyModuleSpecifications()
	{
		Collection<MavenModuleSpecification> moduleSpecifications = new ArrayList<MavenModuleSpecification>();
		for( Dependency dependency : dependencies )
		{
			if( dependency.isOmitted() )
				continue;

			moduleSpecifications.add( new MavenModuleSpecification( dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion(), true ) );
		}
		return Collections.unmodifiableCollection( moduleSpecifications );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<String, String> properties = new HashMap<String, String>();

	private final String parentGroupId;

	private final String parentVersion;

	private final String groupId;

	private final String artifactId;

	private final String version;

	private final String name;

	private final String description;

	private final Collection<Dependency> dependencies = new ArrayList<Dependency>();
}
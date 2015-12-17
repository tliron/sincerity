package com.threecrickets.creel.maven.internal;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.sincerity.util.IoUtil;
import com.threecrickets.sincerity.util.XmlUtil;

public class MetaData
{
	//
	// Construction
	//

	public MetaData( URL url ) throws IOException
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
			throw new RuntimeException( "Invalid metadata", x );
		}

		// <metadata>
		Element metadata = XmlUtil.getFirstElement( document.getDocumentElement(), "metadata" );
		if( metadata == null )
			throw new RuntimeException( "Invalid metadata: no <metadata>" );

		groupId = XmlUtil.getFirstElementText( metadata, "groupId" );
		artifactId = XmlUtil.getFirstElementText( metadata, "artifactId" );

		// <versioning>
		Element versioning = XmlUtil.getFirstElement( metadata, "versioning" );
		if( versioning == null )
		{
			release = null;
			return;
		}

		release = XmlUtil.getFirstElementText( versioning, "release" );

		// <versions>
		Element versions = XmlUtil.getFirstElement( versioning, "versions" );
		if( versions != null )
		{
			// <version>
			for( Element version : new XmlUtil.Elements( versioning, "version" ) )
			{
				String versionString = version.getTextContent();
				this.versions.add( versionString );
			}
		}
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

	public String getRelease()
	{
		return release;
	}

	public Collection<String> getVersions()
	{
		return Collections.unmodifiableCollection( versions );
	}

	public Iterable<MavenModuleIdentifier> getModuleIdentifiers( MavenRepository repository )
	{
		Collection<MavenModuleIdentifier> moduleIdentifiers = new ArrayList<MavenModuleIdentifier>();
		for( String version : versions )
			moduleIdentifiers.add( new MavenModuleIdentifier( repository, groupId, artifactId, version ) );
		return Collections.unmodifiableCollection( moduleIdentifiers );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String groupId;

	private final String artifactId;

	private final String release;

	private final Collection<String> versions = new ArrayList<String>();
}

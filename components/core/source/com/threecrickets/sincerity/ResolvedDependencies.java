package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.License;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ResolvedDependencies extends ArrayList<ResolvedDependency>
{
	//
	// Construction
	//

	public ResolvedDependencies( Dependencies dependencies ) throws ParserConfigurationException, SAXException, IOException
	{
		super();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document document = documentBuilder.parse( dependencies.getResolutionReport() );

		ArrayList<ResolvedDependency> resolvedDependencys = new ArrayList<ResolvedDependency>();

		dependencies.getContainer().getIvy().pushContext();

		Element root = document.getDocumentElement();
		if( "ivy-report".equals( root.getTagName() ) )
		{
			NodeList dependenciesList = root.getElementsByTagName( "dependencies" );
			if( dependenciesList.getLength() > 0 )
			{
				NodeList moduleList = ( (Element) dependenciesList.item( 0 ) ).getElementsByTagName( "module" );
				for( int moduleLength = moduleList.getLength(), moduleIndex = 0; moduleIndex < moduleLength; moduleIndex++ )
				{
					Element module = (Element) moduleList.item( moduleIndex );
					String organisation = module.getAttribute( "organisation" );
					String moduleName = module.getAttribute( "name" );

					NodeList revisionList = module.getElementsByTagName( "revision" );
					for( int revisionLength = revisionList.getLength(), revisionIndex = 0; revisionIndex < revisionLength; revisionIndex++ )
					{
						Element revision = (Element) revisionList.item( revisionIndex );
						String revisionName = revision.getAttribute( "name" );
						String branch = revision.getAttribute( "branch" );
						String homePage = revision.getAttribute( "homepage" );
						String evicted = revision.getAttribute( "evicted" );
						// boolean isDefault = Boolean.valueOf(
						// revision.getAttribute( "default" ) );

						ModuleRevisionId id = ModuleRevisionId.newInstance( organisation, moduleName, branch, revisionName );
						DefaultModuleDescriptor moduleDescriptor = new DefaultModuleDescriptor( id, "release", null );
						moduleDescriptor.addConfiguration( new Configuration( DefaultModuleDescriptor.DEFAULT_CONFIGURATION ) );
						moduleDescriptor.setHomePage( homePage );

						ResolvedDependency resolvedDependency = new ResolvedDependency( moduleDescriptor, evicted );
						resolvedDependencys.add( resolvedDependency );

						NodeList licenseList = revision.getElementsByTagName( "license" );
						for( int licenseLength = licenseList.getLength(), licenseIndex = 0; licenseIndex < licenseLength; licenseIndex++ )
						{
							Element license = (Element) licenseList.item( licenseIndex );
							String licenseName = license.getAttribute( "name" );
							String url = license.getAttribute( "url" );

							License theLicense = new License( licenseName, url );
							moduleDescriptor.addLicense( theLicense );
						}

						NodeList callerList = revision.getElementsByTagName( "caller" );
						for( int callerLength = callerList.getLength(), callerIndex = 0; callerIndex < callerLength; callerIndex++ )
						{
							Element caller = (Element) callerList.item( callerIndex );
							String callerOrganisation = caller.getAttribute( "organisation" );
							String callerName = caller.getAttribute( "name" );
							String callerRev = caller.getAttribute( "callerrev" );

							Caller theCaller = new Caller( callerOrganisation, callerName, callerRev );
							resolvedDependency.callers.add( theCaller );
						}

						NodeList artifactsList = revision.getElementsByTagName( "artifacts" );
						if( artifactsList.getLength() > 0 )
						{
							NodeList artifactList = ( (Element) artifactsList.item( 0 ) ).getElementsByTagName( "artifact" );
							for( int artifactLength = artifactList.getLength(), artifactIndex = 0; artifactIndex < artifactLength; artifactIndex++ )
							{
								Element artifact = (Element) artifactList.item( artifactIndex );
								String artifactName = artifact.getAttribute( "name" );
								String artifactType = artifact.getAttribute( "type" );
								String artifactExt = artifact.getAttribute( "ext" );
								String artifactSize = artifact.getAttribute( "size" );
								String artifactLocation = artifact.getAttribute( "location" );

								HashMap<String, Object> attributes = new HashMap<String, Object>();
								attributes.put( "size", artifactSize );
								attributes.put( "location", artifactLocation );
								DefaultArtifact theArtifact = new DefaultArtifact( id, null, artifactName, artifactType, artifactExt, attributes );
								moduleDescriptor.addArtifact( "default", theArtifact );
							}
						}
					}
				}
			}
		}

		dependencies.getContainer().getIvy().popContext();

		// Build tree
		for( ResolvedDependency resolvedDependency : resolvedDependencys )
		{
			for( Caller caller : resolvedDependency.callers )
			{
				for( ResolvedDependency parentNode : resolvedDependencys )
				{
					ModuleRevisionId parentId = parentNode.descriptor.getModuleRevisionId();
					if( caller.organisation.equals( parentId.getOrganisation() ) && caller.name.equals( parentId.getName() ) && caller.revision.equals( parentId.getRevision() ) )
					{
						parentNode.children.add( resolvedDependency );
						resolvedDependency.isRoot = false;
						break;
					}
				}
			}
		}

		// Gather roots
		for( ResolvedDependency resolvedDependency : resolvedDependencys )
		{
			if( resolvedDependency.isRoot )
				add( resolvedDependency );
		}
	}

	//
	// Attributes
	//

	public Set<URL> getJarUrls2() throws ParseException, MalformedURLException
	{
		HashSet<URL> urls = new HashSet<URL>();
		for( ResolvedDependency resolvedDependency : getInstalledDependencies() )
		{
			for( org.apache.ivy.core.module.descriptor.Artifact artifact : resolvedDependency.descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION ) )
			{
				String location = artifact.getId().getAttribute( "location" );
				if( "jar".equals( artifact.getType() ) && ( location != null ) )
					urls.add( new File( location ).toURI().toURL() );
			}
		}
		return urls;
	}

	//
	// Operations
	//

	public ArrayList<ResolvedDependency> getInstalledDependencies()
	{
		ArrayList<ResolvedDependency> installedDependencies = new ArrayList<ResolvedDependency>();
		for( ResolvedDependency resolvedDependency : this )
			addInstalledDependencies( resolvedDependency, installedDependencies );
		return installedDependencies;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected static class Caller
	{
		private Caller( String organisation, String name, String revision )
		{
			this.organisation = organisation;
			this.name = name;
			this.revision = revision;
		}

		private final String organisation;

		private final String name;

		private final String revision;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private static void addInstalledDependencies( ResolvedDependency resolvedDependency, ArrayList<ResolvedDependency> installedDependencies )
	{
		if( resolvedDependency.evicted != null )
			return;

		boolean exists = false;
		ModuleRevisionId id = resolvedDependency.descriptor.getModuleRevisionId();
		for( ResolvedDependency installedDependency : installedDependencies )
		{
			if( id.equals( installedDependency.descriptor.getModuleRevisionId() ) )
			{
				exists = true;
				break;
			}
		}

		if( !exists )
			installedDependencies.add( resolvedDependency );

		for( ResolvedDependency child : resolvedDependency.children )
			addInstalledDependencies( child, installedDependencies );
	}
}

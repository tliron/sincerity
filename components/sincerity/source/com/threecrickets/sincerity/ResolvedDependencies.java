/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.License;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.threecrickets.sincerity.exception.SincerityException;

public class ResolvedDependencies extends AbstractList<ResolvedDependency>
{
	//
	// Construction
	//

	public ResolvedDependencies( Dependencies dependencies ) throws SincerityException
	{
		super();

		File resolutionReport = dependencies.getResolutionReport();
		if( !resolutionReport.exists() )
			return;

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Document document;
		try
		{
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			document = documentBuilder.parse( resolutionReport );
		}
		catch( ParserConfigurationException x )
		{
			throw new SincerityException( "Could not parse resolution report: " + resolutionReport, x );
		}
		catch( SAXException x )
		{
			throw new SincerityException( "Could not parse resolution report: " + resolutionReport, x );
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not read resolution report: " + resolutionReport, x );
		}

		ArrayList<ResolvedDependency> resolvedDependencies = new ArrayList<ResolvedDependency>();

		dependencies.getContainer().getIvy().pushContext();
		try
		{
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
							resolvedDependencies.add( resolvedDependency );

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
		}
		finally
		{
			dependencies.getContainer().getIvy().popContext();
		}

		// Build tree
		for( ResolvedDependency resolvedDependency : resolvedDependencies )
		{
			for( Caller caller : resolvedDependency.callers )
			{
				for( ResolvedDependency parentNode : resolvedDependencies )
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
		for( ResolvedDependency resolvedDependency : resolvedDependencies )
		{
			if( resolvedDependency.isRoot )
				this.roots.add( resolvedDependency );
		}
	}

	//
	// Attributes
	//

	public List<ResolvedDependency> getAll()
	{
		ArrayList<ResolvedDependency> allDependencies = new ArrayList<ResolvedDependency>();
		for( ResolvedDependency resolvedDependency : this )
			addAllDependencies( resolvedDependency, allDependencies );
		return allDependencies;
	}

	public List<Artifact> getArtifacts()
	{
		ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
		for( ResolvedDependency resolvedDependency : getAll() )
			for( Artifact artifact : resolvedDependency.descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION ) )
				artifacts.add( artifact );
		return artifacts;
	}

	public List<License> getLicenses()
	{
		ArrayList<License> licenses = new ArrayList<License>();
		for( ResolvedDependency resolvedDependency : getAll() )
		{
			for( License license : resolvedDependency.descriptor.getLicenses() )
			{
				boolean exists = false;
				for( License l : licenses )
				{
					if( l.getUrl().equals( license.getUrl() ) )
					{
						exists = true;
						break;
					}
				}
				if( !exists )
					licenses.add( license );
			}
		}
		return licenses;
	}

	public List<ResolvedDependency> getByLicense( License license )
	{
		ArrayList<ResolvedDependency> dependencies = new ArrayList<ResolvedDependency>();
		for( ResolvedDependency resolvedDependency : getAll() )
		{
			for( License l : resolvedDependency.descriptor.getLicenses() )
			{
				if( l.getUrl().equals( license.getUrl() ) )
				{
					dependencies.add( resolvedDependency );
					break;
				}
			}
		}
		return dependencies;
	}

	public String getVersion( String group, String name ) throws SincerityException
	{
		for( ResolvedDependency resolvedDependency : getAll() )
		{
			ModuleRevisionId id = resolvedDependency.descriptor.getModuleRevisionId();
			if( group.equals( id.getOrganisation() ) && name.equals( id.getName() ) )
				return id.getRevision();
		}
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
	public ResolvedDependency get( int index )
	{
		return roots.get( index );
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

	private final List<ResolvedDependency> roots = new ArrayList<ResolvedDependency>();

	private static void addAllDependencies( ResolvedDependency resolvedDependency, ArrayList<ResolvedDependency> installedDependencies )
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
			addAllDependencies( child, installedDependencies );
	}
}

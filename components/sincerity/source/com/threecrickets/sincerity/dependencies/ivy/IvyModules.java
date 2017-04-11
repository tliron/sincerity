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

package com.threecrickets.sincerity.dependencies.ivy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.Configuration;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.threecrickets.sincerity.dependencies.Artifact;
import com.threecrickets.sincerity.dependencies.License;
import com.threecrickets.sincerity.dependencies.Module;
import com.threecrickets.sincerity.dependencies.Modules;
import com.threecrickets.sincerity.dependencies.ivy.IvyModule.Caller;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * This class is essentially a parser for an Ivy resolution report.
 * 
 * @author Tal Liron
 */
public class IvyModules extends Modules<IvyModule>
{
	//
	// Modules
	//

	@Override
	public Collection<IvyModule> getAll()
	{
		if( allModules == null )
		{
			allModules = new ArrayList<IvyModule>();
			for( IvyModule module : this )
				addAllDependencies( module, allModules );
		}
		return allModules;
	}

	@Override
	public Collection<Artifact> getArtifacts()
	{
		if( artifacts == null )
		{
			artifacts = new ArrayList<Artifact>();
			for( IvyModule module : getAll() )
				for( Artifact artifact : module.getArtifacts() )
					artifacts.add( artifact );
		}
		return artifacts;
	}

	@Override
	public Collection<License> getLicenses()
	{
		if( licenses == null )
		{
			licenses = new ArrayList<License>();
			for( IvyModule module : getAll() )
				for( License license : module.getLicenses() )
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

	@Override
	public Collection<IvyModule> getByLicense( License license )
	{
		ArrayList<IvyModule> dependencies = new ArrayList<IvyModule>();
		for( IvyModule module : getAll() )
			for( License l : module.getLicenses() )
			{
				if( l.getUrl().equals( license.getUrl() ) )
				{
					dependencies.add( module );
					break;
				}
			}
		return dependencies;
	}

	@Override
	public String getVersion( String group, String name ) throws SincerityException
	{
		for( IvyModule module : getAll() )
			if( group.equals( module.getGroup() ) && name.equals( module.getName() ) )
				return module.getVersion();
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
	public IvyModule get( int index )
	{
		return roots.get( index );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Parses the latest Ivy resolution report.
	 * 
	 * @param dependencies
	 *        The dependencies instance
	 * @throws SincerityException
	 *         In case of an error
	 */
	protected IvyModules( IvyDependencies dependencies ) throws SincerityException
	{
		File resolutionReport = dependencies.getResolutionReportFile();
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

		ArrayList<IvyModule> modules = new ArrayList<IvyModule>();

		Ivy ivy = ( (IvyContainer) dependencies.getContainer() ).getIvy();
		ivy.pushContext();
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

							IvyModule ivyModule = new IvyModule( moduleDescriptor, evicted );
							modules.add( ivyModule );

							NodeList licenseList = revision.getElementsByTagName( "license" );
							for( int licenseLength = licenseList.getLength(), licenseIndex = 0; licenseIndex < licenseLength; licenseIndex++ )
							{
								Element license = (Element) licenseList.item( licenseIndex );
								String licenseName = license.getAttribute( "name" );
								String url = license.getAttribute( "url" );

								org.apache.ivy.core.module.descriptor.License theLicense = new org.apache.ivy.core.module.descriptor.License( licenseName, url );
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
								ivyModule.callers.add( theCaller );
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
			ivy.popContext();
		}

		// Build tree
		for( IvyModule module : modules )
			for( Caller caller : module.callers )
				for( IvyModule parentModule : modules )
				{
					if( caller.organisation.equals( parentModule.getGroup() ) && caller.name.equals( parentModule.getName() ) && caller.revision.equals( parentModule.getVersion() ) )
					{
						parentModule.getChildren().add( module );
						module.setEplicitDependency( false );
						break;
					}
				}

		// Gather roots
		for( IvyModule module : modules )
			if( module.isExplicitDependency() )
				this.roots.add( module );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Recursively adds a resolved dependency and its children to a list.
	 * 
	 * @param module
	 *        The resolved dependency
	 * @param modules
	 *        The list of dependencies to which we will add
	 */
	private static void addAllDependencies( IvyModule module, ArrayList<IvyModule> modules )
	{
		if( module.isEvicted() )
			return;

		boolean exists = false;
		for( IvyModule existingModule : modules )
			if( module.getGroup().equals( existingModule.getGroup() ) && module.getName().equals( existingModule.getName() ) && module.getVersion().equals( existingModule.getVersion() ) )
			{
				exists = true;
				break;
			}

		if( !exists )
			modules.add( module );

		for( Module child : module.getChildren() )
			addAllDependencies( (IvyModule) child, modules );
	}

	private final List<IvyModule> roots = new ArrayList<IvyModule>();

	private ArrayList<IvyModule> allModules;

	private ArrayList<Artifact> artifacts;

	private ArrayList<License> licenses;
}

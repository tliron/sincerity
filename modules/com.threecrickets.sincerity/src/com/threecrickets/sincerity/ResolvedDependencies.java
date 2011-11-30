package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ivy.Ivy;
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

	public ResolvedDependencies( File reportFile, Ivy ivy ) throws ParserConfigurationException, SAXException, IOException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document document = documentBuilder.parse( reportFile );

		ArrayList<ResolvedDependency> resolvedDependencys = new ArrayList<ResolvedDependency>();

		ivy.pushContext();
		Element root = document.getDocumentElement();
		if( "ivy-report".equals( root.getTagName() ) )
		{
			NodeList dependenciesList = root.getElementsByTagName( "dependencies" );
			if( dependenciesList.getLength() > 0 )
			{
				Element dependencies = (Element) dependenciesList.item( 0 );
				NodeList moduleList = dependencies.getElementsByTagName( "module" );
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
						DefaultModuleDescriptor moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( id );
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
					}
				}
			}
		}
		ivy.popContext();

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
	// Operations
	//

	public ArrayList<ResolvedDependency> getInstalledDependencies()
	{
		ArrayList<ResolvedDependency> installedDependencies = new ArrayList<ResolvedDependency>();
		for( ResolvedDependency resolvedDependency : this )
			addInstalledDependencies( resolvedDependency, installedDependencies );
		return installedDependencies;
	}

	public void printTree( Writer writer )
	{
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		ArrayList<String> patterns = new ArrayList<String>();
		for( ResolvedDependency resolvedDependency : this )
			printTree( printWriter, resolvedDependency, patterns, false );
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

	private static void printTree( PrintWriter writer, ResolvedDependency resolvedDependency, ArrayList<String> patterns, boolean seal )
	{
		int size = patterns.size();
		if( seal )
			patterns.set( size - 1, size < 2 ? " \\" : "   \\" );
		for( String pattern : patterns )
			System.out.print( pattern );
		if( size > 0 )
			System.out.print( "--" );
		if( seal )
			patterns.set( size - 1, size < 2 ? "  " : "    " );

		writer.println( resolvedDependency );

		if( !resolvedDependency.children.isEmpty() )
		{
			patterns.add( size == 0 ? " |" : "   |" );

			for( Iterator<ResolvedDependency> i = resolvedDependency.children.iterator(); i.hasNext(); )
			{
				ResolvedDependency child = i.next();
				printTree( writer, child, patterns, !i.hasNext() );
			}

			patterns.remove( patterns.size() - 1 );
		}
	}
}

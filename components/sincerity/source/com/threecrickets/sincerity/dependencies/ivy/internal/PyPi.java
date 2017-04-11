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

package com.threecrickets.sincerity.dependencies.ivy.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.repository.Repository;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.version.VersionMatcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Handles access to <a href="http://pypi.python.org/">PyPI</a>-compatible
 * software repositories for the Python platform.
 * <p>
 * This code was developed by reverse engineering the Python code in
 * easy_install and pip.
 * <p>
 * Note that pypi.python.org itself does not return modification dates for any
 * of its resources, an unfortunate implementation that makes it difficult to
 * efficiently cache downloads.
 * 
 * @author Tal Liron
 * @see PyPiResolver
 */
public class PyPi
{
	//
	// Constants
	//

	public static final String EGG_MODE_PREFIX = "~";

	public static final int EGG_MODE_PREFIX_LENGTH = EGG_MODE_PREFIX.length();

	//
	// Static operations
	//

	/**
	 * Creates a regular expression to match PyPI artifact filenames and extract
	 * the following unnamed groups:
	 * <p>
	 * Group 1: version<br>
	 * Group 2: python version<br>
	 * Group 3: file extension<br>
	 * 
	 * @param moduleName
	 *        The module name
	 * @return The regular expression
	 */
	public static Pattern getArtifactPattern( String moduleName )
	{
		return Pattern.compile( "^" + Pattern.quote( moduleName ) + "\\-(.+?)(?:\\-py(.+))?\\.(egg|tar\\.gz|tar\\.bz2|tgz|zip)$" );
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param root
	 *        The PyPI root URI
	 * @param repository
	 *        The Ivy repository
	 */
	public PyPi( String root, Repository repository )
	{
		this.root = root;
		this.repository = repository;
	}

	//
	// Operations
	//

	/**
	 * Retrieves and caches all available module names by parsing the HTML of
	 * the index page.
	 * 
	 * @return The module names
	 */
	public List<String> listModuleNames()
	{
		ArrayList<String> moduleNames = new ArrayList<String>();
		Document indexDocument = getIndexDocument();
		if( indexDocument != null )
		{
			Elements moduleLinks = indexDocument.select( "a[href]" );
			for( Element moduleLink : moduleLinks )
				moduleNames.add( moduleLink.text() );
		}
		return moduleNames;
	}

	/**
	 * Retrieves and caches the URI of a module's page by parsing the HTML of
	 * the index page.
	 * 
	 * @param moduleName
	 *        The module name
	 * @return The URI
	 */
	public String getModuleUri( String moduleName )
	{
		String moduleUri = moduleUris.get( moduleName );
		Document indexDocument = getIndexDocument();
		if( indexDocument != null )
		{
			Elements moduleLinks = indexDocument.select( "a[href]" );
			for( Element moduleLink : moduleLinks )
			{
				if( moduleName.equals( moduleLink.text() ) )
				{
					moduleUri = root + moduleLink.attr( "href" );
					moduleUris.put( moduleName, moduleUri );
					break;
				}
			}
		}
		return moduleUri;
	}

	/**
	 * Gets the last modification date for a module.
	 * <p>
	 * Note: PyPI does not keep modification dates for module pages, but we can
	 * give it a try anyway.
	 * 
	 * @param moduleName
	 *        The module name
	 * @return The modification date or the current time if not found
	 */
	public long getModuleLastModified( String moduleName )
	{
		long lastModified = 0;
		Resource moduleResource = getModuleResource( moduleName );
		if( moduleResource != null )
			lastModified = moduleResource.getLastModified();
		if( lastModified == 0 )
			lastModified = System.currentTimeMillis();
		return lastModified;
	}

	/**
	 * Retrieves the list of artifacts for a module by parsing the HTML of its
	 * page.
	 * <p>
	 * Each artifact node is an array of 2 strings, where the first string is
	 * the artifact's name and the second string is the artifact's download UI.
	 * 
	 * @param moduleName
	 *        The module name
	 * @return The artifact names
	 */
	public List<String[]> listArtifacts( String moduleName )
	{
		ArrayList<String[]> artifactNames = new ArrayList<String[]>();

		Document moduleDocument = getModuleDocument( moduleName );
		if( moduleDocument != null )
		{
			String moduleUri = getModuleUri( moduleName );
			Elements artifactLinks = moduleDocument.select( "a[href]" );
			for( Element artifactLink : artifactLinks )
			{
				artifactNames.add( new String[]
				{
					artifactLink.text(), moduleUri + artifactLink.attr( "href" )
				} );
			}
		}

		return artifactNames;
	}

	/**
	 * Retrieves the URI for downloading an artifact by parsing the HTML of the
	 * module's page.
	 * 
	 * @param moduleName
	 *        The module name
	 * @param artifactName
	 *        The artifact name
	 * @return The artifact download URI
	 */
	public String getArtifactUri( String moduleName, String artifactName )
	{
		Document moduleDocument = getModuleDocument( moduleName );
		if( moduleDocument != null )
		{
			String moduleUri = getModuleUri( moduleName );
			Elements artifactLinks = moduleDocument.select( "a[href]" );
			for( Element artifactLink : artifactLinks )
			{
				String text = artifactLink.text();
				if( artifactName.equals( text ) )
					return moduleUri + artifactLink.attr( "href" );
			}
		}
		return null;
	}

	/**
	 * Finds all artifacts matching the conditions.
	 * <p>
	 * Each artifact node is an array of 5 strings:
	 * <p>
	 * 0: name<br>
	 * 1: download URI<br>
	 * 2: version<br>
	 * 3: Python version<br>
	 * 4: file extension
	 * 
	 * @param id
	 *        The Ivy module revision ID
	 * @param versionMatcher
	 *        The Ivy version matcher
	 * @param pythonVersion
	 *        The Python version
	 * @return artifacts The found artifacts
	 */
	public List<String[]> findArtifacts( ModuleRevisionId id, VersionMatcher versionMatcher, String pythonVersion )
	{
		ArrayList<String[]> artifacts = new ArrayList<String[]>();

		String moduleName = id.getName();
		if( moduleName.startsWith( EGG_MODE_PREFIX ) )
			moduleName = moduleName.substring( EGG_MODE_PREFIX_LENGTH );
		Document moduleDocument = getModuleDocument( moduleName );
		if( moduleDocument != null )
		{
			String moduleUri = getModuleUri( moduleName );
			Pattern pattern = getArtifactPattern( moduleName );
			Elements artifactLinks = moduleDocument.select( "a[href]" );
			for( Element artifactLink : artifactLinks )
			{
				String text = artifactLink.text();
				Matcher matcher = pattern.matcher( text );
				if( matcher.find() )
				{
					ModuleRevisionId foundId = ModuleRevisionId.newInstance( id, matcher.group( 1 ) );
					if( versionMatcher.accept( id, foundId ) )
					{
						boolean ok = true;
						if( pythonVersion != null && matcher.group( 2 ) != null )
							ok = pythonVersion.equals( matcher.group( 2 ) );
						if( ok )
						{
							artifacts.add( new String[]
							{
								text, moduleUri + artifactLink.attr( "href" ), matcher.group( 1 ), matcher.group( 2 ), matcher.group( 3 )
							} );
						}
					}
				}
			}
		}

		return artifacts;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String root;

	private final Repository repository;

	private Document indexDocument;

	private Map<String, Resource> moduleResources = new HashMap<String, Resource>();

	private Map<String, Document> moduleDocuments = new HashMap<String, Document>();

	private Map<String, String> moduleUris = new HashMap<String, String>();

	/**
	 * Retrieves the index page, parses its HTML, and caches the parsed result.
	 * 
	 * @return The parsed index page
	 */
	private Document getIndexDocument()
	{
		if( indexDocument == null )
		{
			try
			{
				Resource indexResource = repository.getResource( root );
				InputStream indexStream = indexResource.openStream();
				try
				{
					indexDocument = Jsoup.parse( indexStream, null, root );
				}
				finally
				{
					indexStream.close();
				}
			}
			catch( Exception x )
			{
				x.printStackTrace();
			}
		}

		return indexDocument;
	}

	/**
	 * The cached Ivy resource for the module.
	 * 
	 * @param moduleName
	 *        The module name
	 * @return The Ivy resource
	 */
	private Resource getModuleResource( String moduleName )
	{
		Resource moduleResource = moduleResources.get( moduleName );
		if( moduleResource == null )
		{
			try
			{
				String moduleUri = getModuleUri( moduleName );
				if( moduleUri != null )
				{
					moduleResource = repository.getResource( moduleUri );
					moduleResources.put( moduleName, moduleResource );
				}
			}
			catch( Exception x )
			{
				x.printStackTrace();
			}
		}

		return moduleResource;
	}

	/**
	 * The cached parsed HTML of the module's page.
	 * 
	 * @param moduleName
	 *        The module name
	 * @return The parsed page
	 */
	private Document getModuleDocument( String moduleName )
	{
		Document moduleDocument = moduleDocuments.get( moduleName );
		if( moduleDocument == null )
		{
			try
			{
				Resource moduleResource = getModuleResource( moduleName );
				if( moduleResource != null )
				{
					InputStream moduleStream = moduleResource.openStream();
					try
					{
						moduleDocument = Jsoup.parse( moduleStream, null, getModuleUri( moduleName ) );
						moduleDocuments.put( moduleName, moduleDocument );
					}
					finally
					{
						moduleStream.close();
					}
				}
			}
			catch( Exception x )
			{
				x.printStackTrace();
			}
		}

		return moduleDocument;
	}
}

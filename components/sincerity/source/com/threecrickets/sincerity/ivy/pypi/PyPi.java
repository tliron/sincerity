package com.threecrickets.sincerity.ivy.pypi;

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
	 * Group 1: version<br/>
	 * Group 2: python version<br/>
	 * Group 3: file extension<br/>
	 * 
	 * @param moduleName
	 * @return pattern
	 */
	public static Pattern getArtifactPattern( String moduleName )
	{
		return Pattern.compile( "^" + Pattern.quote( moduleName ) + "\\-(.+?)(?:\\-py(.+))?\\.(egg|tar\\.gz|tar\\.bz2|tgz|zip)$" );
	}

	//
	// Construction
	//

	public PyPi( String root, Repository repository )
	{
		this.root = root;
		this.repository = repository;
	}

	//
	// Operations
	//

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

	public long getModuleLastModified( String moduleName )
	{
		// Note: PyPI does not keep modification dates for module pages, but
		// we'll give it a try anyway
		long lastModified = 0;
		Resource moduleResource = getModuleResource( moduleName );
		if( moduleResource != null )
			lastModified = moduleResource.getLastModified();
		if( lastModified == 0 )
			lastModified = System.currentTimeMillis();
		return lastModified;
	}

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
	 * 0: name<br/>
	 * 1: uri<br/>
	 * 2: version<br/>
	 * 3: python version<br/>
	 * 4: file extension<br/>
	 * 
	 * @param id
	 * @param versionMatcher
	 * @param pythonVersion
	 * @return artifacts
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

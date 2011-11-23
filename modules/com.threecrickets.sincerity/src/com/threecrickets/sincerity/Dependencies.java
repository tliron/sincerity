package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.License;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.report.XmlReportParser;
import org.apache.ivy.plugins.repository.url.URLResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.threecrickets.sincerity.internal.XmlUtil;

public class Dependencies
{
	//
	// Classes
	//

	public static class Node
	{
		public final ModuleDescriptor descriptor;

		public final ArrayList<Node> children = new ArrayList<Node>();

		// //////////////////////////////////////////////////////////////////////////
		// Private

		private Node( ModuleDescriptor descriptor )
		{
			this.descriptor = descriptor;
		}

		private boolean isRoot = true;

		private final ArrayList<Caller> callers = new ArrayList<Caller>();
	}

	//
	// Construction
	//

	public Dependencies( File ivyFile, File artifactsFile, Container container ) throws IOException, ParseException
	{
		this.ivyFile = ivyFile;
		this.artifacts = new Artifacts( artifactsFile );
		this.container = container;
		this.ivy = container.getIvy();

		// Module
		if( ivyFile.exists() )
		{
			URL ivyUrl = ivyFile.toURI().toURL();
			URLResource resource = new URLResource( ivyUrl );
			ModuleDescriptorParser parser = ModuleDescriptorParserRegistry.getInstance().getParser( resource );
			ivy.pushContext();
			moduleDescriptor = (DefaultModuleDescriptor) parser.parseDescriptor( ivy.getSettings(), ivyUrl, true );
			ivy.popContext();
		}
		else
		{
			ivy.pushContext();
			moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( ModuleRevisionId.newInstance( "threecrickets", "sincerity-container", "working" ) );
			ivy.popContext();
		}

		// Default resolve options
		defaultResolveOptions = new ResolveOptions();
		defaultResolveOptions.setConfs( new String[]
		{
			"default"
		} );
		// defaultResolveOptions.setOutputReport( false );
		defaultResolveOptions.setLog( LogOptions.LOG_QUIET );
	}

	//
	// Attributes
	//

	public Packages getPackages() throws ParseException, MalformedURLException, IOException
	{
		return new Packages( container.getRoot(), getClassLoader() );
	}

	public boolean has( String organisation, String name, String revision )
	{
		return has( ModuleRevisionId.newInstance( organisation, name, revision ) );
	}

	public boolean has( ModuleRevisionId id )
	{
		for( DependencyDescriptor dependency : moduleDescriptor.getDependencies() )
		{
			if( id.equals( dependency.getDependencyRevisionId() ) )
				return true;
		}
		return false;
	}

	public DependencyDescriptor[] getDescriptors()
	{
		return moduleDescriptor.getDependencies();
	}

	public List<Node> getDescriptorTree() throws ParserConfigurationException, SAXException, IOException
	{
		ArrayList<Node> nodes = new ArrayList<Node>();

		ivy.pushContext();
		Document document = getResolutionReportDocument();
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
						// boolean isDefault = Boolean.valueOf(
						// revision.getAttribute( "default" ) );

						ModuleRevisionId id = ModuleRevisionId.newInstance( organisation, moduleName, branch, revisionName );
						DefaultModuleDescriptor moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( id );
						moduleDescriptor.setHomePage( homePage );

						Node node = new Node( moduleDescriptor );
						nodes.add( node );

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
							node.callers.add( theCaller );
						}
					}
				}
			}
		}
		ivy.popContext();

		// Build node tree
		for( Node node : nodes )
		{
			for( Caller caller : node.callers )
			{
				for( Node parentNode : nodes )
				{
					ModuleRevisionId parentId = parentNode.descriptor.getModuleRevisionId();
					if( caller.organisation.equals( parentId.getOrganisation() ) && caller.name.equals( parentId.getName() ) && caller.revision.equals( parentId.getRevision() ) )
					{
						parentNode.children.add( node );
						node.isRoot = false;
						break;
					}
				}
			}
		}

		// Gather root nodes
		ArrayList<Node> rootNodes = new ArrayList<Node>();
		for( Node node : nodes )
		{
			if( node.isRoot )
				rootNodes.add( node );
		}

		return rootNodes;
	}

	public Set<Artifact> getArtifacts() throws ParseException, MalformedURLException, IOException
	{
		return getArtifacts( false, false );
	}

	public Set<Artifact> getArtifacts( boolean unpack, boolean overwrite ) throws ParseException, MalformedURLException, IOException
	{
		HashSet<Artifact> artifacts = new HashSet<Artifact>();

		for( ArtifactDownloadReport downloadReport : getDownloadReports() )
		{
			if( downloadReport.getLocalFile() != null )
				artifacts.add( new Artifact( downloadReport.getLocalFile().getAbsoluteFile(), null ) );
		}

		Packages packages = getPackages();
		for( Package pack : packages.values() )
		{
			for( Artifact artifact : pack )
			{
				if( unpack && !this.artifacts.isPresent( artifact ) )
					artifact.unpack( overwrite );

				artifacts.add( artifact );
			}
		}

		return artifacts;
	}

	public Set<URL> getJarUrls() throws ParseException, MalformedURLException
	{
		HashSet<URL> urls = new HashSet<URL>();
		for( ArtifactDownloadReport artifact : getDownloadReports() )
		{
			if( "jar".equals( artifact.getType() ) && ( artifact.getLocalFile() != null ) )
				urls.add( artifact.getLocalFile().getAbsoluteFile().toURI().toURL() );
		}
		return urls;
	}

	public ClassLoader getClassLoader() throws MalformedURLException, ParseException
	{
		if( lastResolveReport != null )
		{
			Set<URL> urls = getJarUrls();
			ClassLoader classLoader = new URLClassLoader( urls.toArray( new URL[urls.size()] ), Dependencies.class.getClassLoader() );
			Thread.currentThread().setContextClassLoader( classLoader );
			return classLoader;
		}
		else
			return Dependencies.class.getClassLoader();
	}

	//
	// Operations
	//

	public boolean add( String organisation, String name, String revision ) throws ParseException, IOException
	{
		ModuleRevisionId id = ModuleRevisionId.newInstance( organisation, name, revision );
		if( has( id ) )
			return false;

		DefaultDependencyDescriptor dependency = new DefaultDependencyDescriptor( moduleDescriptor, id, false, false, true );
		dependency.addDependencyConfiguration( "default", "*" );
		moduleDescriptor.addDependency( dependency );
		save();

		return true;
	}

	public boolean remove( String organisation, String name, String revision ) throws ParseException, IOException
	{
		ModuleRevisionId id = ModuleRevisionId.newInstance( organisation, name, revision );
		List<DependencyDescriptor> dependencies = new ArrayList<DependencyDescriptor>( Arrays.asList( moduleDescriptor.getDependencies() ) );
		boolean removed = false;
		for( Iterator<DependencyDescriptor> i = dependencies.iterator(); i.hasNext(); )
		{
			if( id.equals( i.next().getDependencyRevisionId() ) )
			{
				i.remove();
				removed = true;
				break;
			}
		}

		if( !removed )
			return false;

		moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( moduleDescriptor.getModuleRevisionId() );
		for( DependencyDescriptor dependency : dependencies )
			moduleDescriptor.addDependency( dependency );
		save();

		return true;
	}

	public void clean() throws ParseException, IOException
	{
		artifacts.update( getArtifacts(), Artifacts.MODE_CLEAN );
	}

	public void prune() throws ParseException, IOException
	{
		artifacts.update( getArtifacts(), Artifacts.MODE_PRUNE );
	}

	public void reset() throws IOException
	{
		ivy.pushContext();
		moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( moduleDescriptor.getModuleRevisionId() );
		ivy.popContext();
		save();
	}

	public void resolve( boolean overwrite ) throws ParseException, IOException
	{
		message( "Resolving..." );

		ivy.pushContext();
		lastResolveReport = ivy.resolve( moduleDescriptor, defaultResolveOptions );
		ivy.popContext();

		artifacts.update( getArtifacts( true, overwrite ), Artifacts.MODE_UPDATE_ONLY );

		message( "Resolved!" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File ivyFile;

	private final Artifacts artifacts;

	private final Container container;

	private final Ivy ivy;

	private final ResolveOptions defaultResolveOptions;

	private DefaultModuleDescriptor moduleDescriptor;

	private ResolveReport lastResolveReport;

	private void message( String message )
	{
		System.out.println( message );
	}

	private void save() throws IOException
	{
		XmlModuleDescriptorWriter.write( moduleDescriptor, XmlUtil.COMMENT_FULL, ivyFile );
	}

	private File getResolutionReport()
	{
		ivy.pushContext();
		ResolutionCacheManager resolutionCache = ivy.getResolutionCacheManager();
		ivy.popContext();
		String resolveId = ResolveOptions.getDefaultResolveId( moduleDescriptor );
		return resolutionCache.getConfigurationResolveReportInCache( resolveId, "default" );
	}

	private XmlReportParser getParsedResolutionReport() throws ParseException
	{
		File reportFile = getResolutionReport();
		if( reportFile.exists() )
		{
			XmlReportParser parser = new XmlReportParser();
			parser.parse( reportFile );
			return parser;
		}
		return null;
	}

	private Document getResolutionReportDocument() throws ParserConfigurationException, SAXException, IOException
	{
		File reportFile = getResolutionReport();
		if( reportFile.exists() )
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			return documentBuilder.parse( reportFile );
		}
		return null;
	}

	/**
	 * Valid from last {@link #resolve()}.
	 * 
	 * @return
	 * @throws ParseException
	 */
	private Set<ArtifactDownloadReport> getDownloadReports() throws ParseException
	{
		HashSet<ArtifactDownloadReport> artifacts = new HashSet<ArtifactDownloadReport>();
		XmlReportParser parser = getParsedResolutionReport();
		if( parser != null )
			artifacts.addAll( Arrays.asList( parser.getArtifactReports() ) );
		return artifacts;
	}

	private static class Caller
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
}

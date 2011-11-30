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

import javax.xml.parsers.ParserConfigurationException;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.report.XmlReportParser;
import org.apache.ivy.plugins.repository.url.URLResource;
import org.xml.sax.SAXException;

import com.threecrickets.sincerity.internal.XmlUtil;

public class Dependencies
{
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

	public ResolvedDependencies getResolvedDependencies() throws ParserConfigurationException, SAXException, IOException
	{
		return new ResolvedDependencies( getResolutionReport(), ivy );
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

	public void install( boolean overwrite ) throws ParseException, IOException
	{
		message( "Installing..." );

		ivy.pushContext();
		lastResolveReport = ivy.resolve( moduleDescriptor, defaultResolveOptions );
		ivy.popContext();

		artifacts.update( getArtifacts( true, overwrite ), Artifacts.MODE_UPDATE_ONLY );

		message( "Installed!" );
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
}

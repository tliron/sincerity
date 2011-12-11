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
import java.util.ListIterator;
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
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.report.XmlReportParser;
import org.apache.ivy.plugins.repository.url.URLResource;
import org.xml.sax.SAXException;

import com.threecrickets.sincerity.internal.NativeUtil;
import com.threecrickets.sincerity.internal.XmlUtil;

public class Dependencies
{
	//
	// Construction
	//

	public Dependencies( File ivyFile, File artifactsFile, Container container ) throws IOException, ParseException
	{
		this.ivyFile = ivyFile;
		this.container = container;
		ivy = container.getIvy();
		installedArtifacts = new InstalledArtifacts( artifactsFile, container );

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

	public Container getContainer()
	{
		return container;
	}

	public Packages getPackages() throws ParseException, MalformedURLException, IOException, ParserConfigurationException, SAXException
	{
		return new Packages( container.getRoot(), getClassLoader(), container );
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
		if( resolvedDependencies == null )
			resolvedDependencies = new ResolvedDependencies( this );
		return resolvedDependencies;
	}

	public ClassLoader getClassLoader() throws MalformedURLException, ParseException, ParserConfigurationException, IOException, SAXException
	{
		if( classLoader == null )
		{
			Set<URL> urls = new HashSet<URL>();

			// Classes directory
			File classesFile = new File( container.getRoot(), "libraries/classes/" );
			if( classesFile.isDirectory() )
				urls.add( classesFile.getAbsoluteFile().toURI().toURL() );

			// Jar artifacts
			urls.addAll( getJarUrls() );

			if( urls.isEmpty() )
				classLoader = Thread.currentThread().getContextClassLoader();
			else
				classLoader = new URLClassLoader( urls.toArray( new URL[urls.size()] ), Thread.currentThread().getContextClassLoader() );

			Thread.currentThread().setContextClassLoader( classLoader );

			NativeUtil.addNativePath( new File( container.getRoot(), "libraries/native" ) );
		}

		return classLoader;
	}

	public File getResolutionReport()
	{
		ivy.pushContext();
		ResolutionCacheManager resolutionCache = ivy.getResolutionCacheManager();
		ivy.popContext();
		String resolveId = ResolveOptions.getDefaultResolveId( moduleDescriptor );
		return resolutionCache.getConfigurationResolveReportInCache( resolveId, "default" );
	}

	public Set<Artifact> getArtifacts() throws ParseException, MalformedURLException, IOException, ParserConfigurationException, SAXException
	{
		return getArtifacts( false, false );
	}

	public Set<Artifact> getArtifacts( boolean unpack, boolean overwrite ) throws ParseException, MalformedURLException, IOException, ParserConfigurationException, SAXException
	{
		HashSet<Artifact> artifacts = new HashSet<Artifact>();

		for( ArtifactDownloadReport downloadReport : getDownloadReports() )
		{
			if( downloadReport.getLocalFile() != null )
				artifacts.add( new Artifact( downloadReport.getLocalFile().getAbsoluteFile(), null, container ) );
		}

		Packages packages = getPackages();
		for( Package pack : packages.values() )
		{
			for( Artifact artifact : pack )
			{
				if( unpack && !installedArtifacts.isPresent( artifact ) )
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
			if( "jar".equals( artifact.getType() ) )
			{
				File file = artifact.getLocalFile();
				if( file != null )
					urls.add( file.getAbsoluteFile().toURI().toURL() );
			}
		}

		// TODO: This should be recursive
		File jarDir = new File( container.getRoot(), "libraries/jars" );
		if( jarDir.isDirectory() )
		{
			for( File file : jarDir.listFiles() )
			{
				if( file.getPath().endsWith( ".jar" ) )
					urls.add( file.getAbsoluteFile().toURI().toURL() );
			}
		}

		return urls;
	}

	//
	// Operations
	//

	public void reset() throws IOException
	{
		ivy.pushContext();
		moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( moduleDescriptor.getModuleRevisionId() );
		File resolutionReport = getResolutionReport();
		if( resolutionReport.exists() )
			resolutionReport.delete();
		ivy.popContext();
		save();
	}

	public boolean add( String group, String name, String version ) throws ParseException, IOException
	{
		ModuleRevisionId id = ModuleRevisionId.newInstance( group, name, version );
		if( has( id ) )
			return false;

		DefaultDependencyDescriptor dependency = new DefaultDependencyDescriptor( moduleDescriptor, id, false, false, true );
		dependency.addDependencyConfiguration( "default", "*" );
		moduleDescriptor.addDependency( dependency );
		save();

		return true;
	}

	public boolean revise( String group, String name, String version ) throws ParseException, IOException
	{
		List<DependencyDescriptor> dependencies = new ArrayList<DependencyDescriptor>( Arrays.asList( moduleDescriptor.getDependencies() ) );
		boolean changed = false;
		for( ListIterator<DependencyDescriptor> i = dependencies.listIterator(); i.hasNext(); )
		{
			DependencyDescriptor dependency = i.next();
			ModuleRevisionId id = dependency.getDependencyRevisionId();
			if( group.equals( id.getOrganisation() ) && name.equals( id.getName() ) && !version.equals( id.getRevision() ) )
			{
				i.remove();
				id = ModuleRevisionId.newInstance( id, version );
				dependency = dependency.clone( id );
				i.add( dependency );
				changed = true;
				break;
			}
		}

		if( !changed )
			return false;

		ivy.pushContext();
		moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( moduleDescriptor.getModuleRevisionId() );
		for( DependencyDescriptor dependency : dependencies )
			moduleDescriptor.addDependency( dependency );
		ivy.popContext();

		save();

		return true;
	}

	public boolean remove( String group, String name ) throws ParseException, IOException
	{
		List<DependencyDescriptor> dependencies = new ArrayList<DependencyDescriptor>( Arrays.asList( moduleDescriptor.getDependencies() ) );
		boolean removed = false;
		for( Iterator<DependencyDescriptor> i = dependencies.iterator(); i.hasNext(); )
		{
			ModuleRevisionId id = i.next().getDependencyRevisionId();
			if( group.equals( id.getOrganisation() ) && name.equals( id.getName() ) )
			{
				i.remove();
				removed = true;
				break;
			}
		}

		if( !removed )
			return false;

		ivy.pushContext();
		moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( moduleDescriptor.getModuleRevisionId() );
		for( DependencyDescriptor dependency : dependencies )
			moduleDescriptor.addDependency( dependency );
		ivy.popContext();

		save();

		return true;
	}

	public void clean() throws ParseException, IOException, ParserConfigurationException, SAXException
	{
		installedArtifacts.update( getArtifacts(), InstalledArtifacts.MODE_CLEAN );
		classLoader = null;
	}

	public void prune() throws ParseException, IOException, ParserConfigurationException, SAXException
	{
		installedArtifacts.update( getArtifacts(), InstalledArtifacts.MODE_PRUNE );
		classLoader = null;
	}

	public void install( boolean overwrite ) throws ParseException, IOException, ParserConfigurationException, SAXException
	{
		System.out.println( "Resolving dependencies" );
		ivy.pushContext();
		ivy.resolve( moduleDescriptor, defaultResolveOptions );
		ivy.popContext();
		classLoader = null;
		installedArtifacts.update( getArtifacts( true, overwrite ), InstalledArtifacts.MODE_PRUNE );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File ivyFile;

	private final InstalledArtifacts installedArtifacts;

	private final Container container;

	private final Ivy ivy;

	private final ResolveOptions defaultResolveOptions;

	private DefaultModuleDescriptor moduleDescriptor;

	private ResolvedDependencies resolvedDependencies;

	private ClassLoader classLoader;

	private void save() throws IOException
	{
		XmlModuleDescriptorWriter.write( moduleDescriptor, XmlUtil.COMMENT_FULL, ivyFile );
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
	 * Valid from last {@link #install()}.
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

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

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.NativeUtil;
import com.threecrickets.sincerity.internal.StringUtil;
import com.threecrickets.sincerity.internal.XmlUtil;

public class Dependencies
{
	//
	// Construction
	//

	public Dependencies( File ivyFile, File artifactsFile, Container container ) throws SincerityException
	{
		this.ivyFile = ivyFile;
		this.container = container;
		ivy = container.getIvy();
		installedArtifacts = new InstalledArtifacts( artifactsFile, container );

		// Module
		if( ivyFile.exists() )
		{
			ivy.pushContext();
			try
			{
				URL ivyUrl = ivyFile.toURI().toURL();
				URLResource resource = new URLResource( ivyUrl );
				ModuleDescriptorParser parser = ModuleDescriptorParserRegistry.getInstance().getParser( resource );
				moduleDescriptor = (DefaultModuleDescriptor) parser.parseDescriptor( ivy.getSettings(), ivyUrl, true );
			}
			catch( MalformedURLException x )
			{
				throw new RuntimeException( x );
			}
			catch( ParseException x )
			{
				throw new SincerityException( "Could not parse dependencies configuration: " + ivyFile, x );
			}
			catch( IOException x )
			{
				throw new SincerityException( "Could not read dependencies configuration: " + ivyFile, x );
			}
			finally
			{
				ivy.popContext();
			}
		}
		else
		{
			ivy.pushContext();
			try
			{
				moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( ModuleRevisionId.newInstance( "threecrickets", "sincerity-container", "working" ) );
			}
			finally
			{
				ivy.popContext();
			}
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

	public Packages getPackages() throws SincerityException
	{
		return new Packages( container );
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

	public ResolvedDependencies getResolvedDependencies() throws SincerityException
	{
		if( resolvedDependencies == null )
			resolvedDependencies = new ResolvedDependencies( this );
		return resolvedDependencies;
	}

	public ClassLoader getClassLoader() throws SincerityException
	{
		if( classLoader == null )
		{
			Set<URL> urls = new HashSet<URL>();
			for( File file : getClasspaths() )
			{
				try
				{
					urls.add( file.toURI().toURL() );
				}
				catch( MalformedURLException x )
				{
					throw new RuntimeException( x );
				}
			}

			if( urls.isEmpty() )
				classLoader = Thread.currentThread().getContextClassLoader();
			else
			{
				classLoader = new URLClassLoader( urls.toArray( new URL[urls.size()] ), Thread.currentThread().getContextClassLoader() );
				Thread.currentThread().setContextClassLoader( classLoader );
			}

			File nativeDir = container.getFile( "libraries", "native" );
			if( nativeDir.isDirectory() )
				NativeUtil.addNativePath( nativeDir );
		}

		return classLoader;
	}

	public File getResolutionReport()
	{
		ivy.pushContext();
		ResolutionCacheManager resolutionCache;
		try
		{
			resolutionCache = ivy.getResolutionCacheManager();
		}
		finally
		{
			ivy.popContext();
		}
		String resolveId = ResolveOptions.getDefaultResolveId( moduleDescriptor );
		return resolutionCache.getConfigurationResolveReportInCache( resolveId, "default" );
	}

	public Set<Artifact> getArtifacts() throws SincerityException
	{
		return getArtifacts( false, false );
	}

	public Set<Artifact> getArtifacts( boolean install, boolean overwrite ) throws SincerityException
	{
		HashSet<Artifact> artifacts = new HashSet<Artifact>();

		for( ArtifactDownloadReport downloadReport : getDownloadReports() )
		{
			if( downloadReport.getLocalFile() != null )
				artifacts.add( new Artifact( downloadReport.getLocalFile().getAbsoluteFile(), null, container ) );
		}

		for( Package pack : getPackages() )
		{
			for( Artifact artifact : pack )
			{
				if( install && !installedArtifacts.isMarkedAsPresent( artifact ) )
					artifact.unpack( null, overwrite );

				artifacts.add( artifact );
			}

			if( install )
				pack.install();
		}

		return artifacts;
	}

	public String getClasspath() throws SincerityException
	{
		List<File> classpaths = getClasspaths();
		ArrayList<String> paths = new ArrayList<String>( classpaths.size() );
		for( File file : classpaths )
			paths.add( file.getPath() );
		return StringUtil.join( paths, ":" );
	}

	public List<File> getClasspaths() throws SincerityException
	{
		ArrayList<File> classpaths = new ArrayList<File>();

		// Classes directory
		File classesDir = container.getFile( "libraries", "classes" );
		if( classesDir.isDirectory() )
			classpaths.add( classesDir );

		// Downloaded artifacts
		for( ArtifactDownloadReport artifact : getDownloadReports() )
		{
			if( "jar".equals( artifact.getType() ) )
			{
				File file = artifact.getLocalFile();
				if( file != null )
					classpaths.add( file.getAbsoluteFile() );
			}
		}

		// TODO: This should be recursive
		File jarDir = container.getFile( "libraries", "jars" );
		if( jarDir.isDirectory() )
		{
			for( File file : jarDir.listFiles() )
			{
				if( file.getPath().endsWith( ".jar" ) )
					classpaths.add( file.getAbsoluteFile() );
			}
		}

		return classpaths;
	}

	//
	// Operations
	//

	public void reset() throws SincerityException
	{
		ivy.pushContext();
		try
		{
			moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( moduleDescriptor.getModuleRevisionId() );
			File resolutionReport = getResolutionReport();
			if( resolutionReport.exists() )
				resolutionReport.delete();
		}
		finally
		{
			ivy.popContext();
		}
		save();
	}

	public boolean add( String group, String name, String version ) throws SincerityException
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

	public boolean revise( String group, String name, String version ) throws SincerityException
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
		try
		{
			moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( moduleDescriptor.getModuleRevisionId() );
			for( DependencyDescriptor dependency : dependencies )
				moduleDescriptor.addDependency( dependency );
		}
		finally
		{
			ivy.popContext();
		}

		save();

		return true;
	}

	public boolean remove( String group, String name ) throws SincerityException
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
		try
		{
			moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( moduleDescriptor.getModuleRevisionId() );
			for( DependencyDescriptor dependency : dependencies )
				moduleDescriptor.addDependency( dependency );
		}
		finally
		{
			ivy.popContext();
		}

		save();

		return true;
	}

	public void clean() throws SincerityException
	{
		installedArtifacts.update( getArtifacts(), InstalledArtifacts.MODE_CLEAN );
		classLoader = null;
	}

	public void prune() throws SincerityException
	{
		installedArtifacts.update( getArtifacts(), InstalledArtifacts.MODE_PRUNE );
		classLoader = null;
	}

	public void install( boolean overwrite ) throws SincerityException
	{
		// Make sure class loader is set
		getClassLoader();

		System.out.println( "Resolving dependencies" );
		ivy.pushContext();
		try
		{
			ivy.resolve( moduleDescriptor, defaultResolveOptions );
		}
		catch( ParseException x )
		{
			throw new SincerityException( "Parsing error while resolving dependencies", x );
		}
		catch( IOException x )
		{
			throw new SincerityException( "I/O error while resolving dependencies", x );
		}
		finally
		{
			ivy.popContext();
		}

		reload();

		installedArtifacts.update( getArtifacts( true, overwrite ), InstalledArtifacts.MODE_PRUNE );
	}

	public void reload()
	{
		classLoader = null;
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

	private void save() throws SincerityException
	{
		try
		{
			XmlModuleDescriptorWriter.write( moduleDescriptor, XmlUtil.COMMENT_FULL, ivyFile );
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not write to dependencies configuration: " + ivyFile, x );
		}
	}

	private XmlReportParser getParsedResolutionReport() throws SincerityException
	{
		File reportFile = getResolutionReport();
		if( reportFile.exists() )
		{
			XmlReportParser parser = new XmlReportParser();
			try
			{
				parser.parse( reportFile );
			}
			catch( ParseException x )
			{
				throw new SincerityException( "Could not parse resolution report: " + reportFile, x );
			}
			return parser;
		}
		return null;
	}

	private Set<ArtifactDownloadReport> getDownloadReports() throws SincerityException
	{
		HashSet<ArtifactDownloadReport> artifacts = new HashSet<ArtifactDownloadReport>();
		XmlReportParser parser = getParsedResolutionReport();
		if( parser != null )
			artifacts.addAll( Arrays.asList( parser.getArtifactReports() ) );
		return artifacts;
	}
}

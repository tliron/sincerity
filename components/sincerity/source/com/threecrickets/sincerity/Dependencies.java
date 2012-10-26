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
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.report.XmlReportParser;
import org.apache.ivy.plugins.repository.url.URLResource;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.FileUtil;
import com.threecrickets.sincerity.internal.StringUtil;
import com.threecrickets.sincerity.internal.XmlUtil;

/**
 * Manages the dependencies of a {@link Container}, including its classpath of
 * Jars as well as other managed artifacts. With this class you can add or
 * remove dependencies to a Sincerity container.
 * <p>
 * Changes to dependencies are only actually resolved when
 * {@link #install(boolean)} is called. To access the actually resolved
 * dependencies, see {@link #getResolvedDependencies()}.
 * <p>
 * To access the Jars, use {@link #getClasspaths(boolean)}. To access the
 * artifacts, use {@link #getArtifacts()} or {@link #getPackages()}. See also
 * the {@link ManagedArtifacts} class.
 * <p>
 * Because the set of plugins in a container depends on its classpath, this is
 * also where you can access them, via {@link #getPlugins()}.
 * <p>
 * The Ivy configuration is stored in
 * "/configuration/sincerity/dependencies.conf". For low-level access to the Ivy
 * descriptors, see {@link #getDescriptors()}.
 * 
 * @author Tal Liron
 */
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
		managedArtifacts = new ManagedArtifacts( artifactsFile, container );

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
		defaultResolveOptions.setResolveMode( ResolveOptions.RESOLVEMODE_DYNAMIC );
		defaultResolveOptions.setConfs( new String[]
		{
			"default"
		} );
		defaultResolveOptions.setCheckIfChanged( true );
		defaultResolveOptions.setLog( container.getSincerity().getVerbosity() >= 1 ? LogOptions.LOG_DEFAULT : LogOptions.LOG_QUIET );
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

	public boolean has( String group, String name )
	{
		return has( group, name, null );
	}

	public boolean has( String group, String name, String version )
	{
		for( DependencyDescriptor dependency : moduleDescriptor.getDependencies() )
		{
			ModuleRevisionId id = dependency.getDependencyRevisionId();
			if( group.equals( id.getOrganisation() ) && name.equals( id.getName() ) && ( ( version == null ) || ( version.equals( id.getRevision() ) ) ) )
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
				artifacts.add( new Artifact( downloadReport.getLocalFile().getAbsoluteFile(), null, false, container ) );
		}

		Packages packages = getPackages();

		for( Package pack : packages )
		{
			for( Artifact artifact : pack )
			{
				if( install && !( artifact.isVolatile() && managedArtifacts.wasInstalled( artifact ) ) )
					artifact.install( null, overwrite );

				artifacts.add( artifact );
			}
		}

		if( install )
			for( Package pack : packages )
				pack.install();

		return artifacts;
	}

	public String getClasspath( boolean includeSystem ) throws SincerityException
	{
		List<File> classpaths = getClasspaths( includeSystem );
		ArrayList<String> paths = new ArrayList<String>( classpaths.size() );
		for( File file : classpaths )
			paths.add( file.getPath() );
		return StringUtil.join( paths, File.pathSeparator );
	}

	public List<File> getClasspaths( boolean includeSystem ) throws SincerityException
	{
		ArrayList<File> classpaths = new ArrayList<File>();

		if( includeSystem )
		{
			// Add JVM classpath
			String system = System.getProperty( "java.class.path" );
			if( system != null )
			{
				for( String path : system.split( File.pathSeparator ) )
				{
					File file = new File( path );
					if( !classpaths.contains( file ) )
						classpaths.add( file );
				}
			}

			// Add master bootstrap
			for( URL url : Bootstrap.getMasterBootstrap().getURLs() )
			{
				if( "file".equals( url.getProtocol() ) )
				{
					try
					{
						File file = new File( url.toURI() );
						if( !classpaths.contains( file ) )
							classpaths.add( file );
					}
					catch( URISyntaxException x )
					{
					}
				}
			}
		}

		// Classes directory
		File classesDir = container.getLibrariesFile( "classes" );
		if( classesDir.isDirectory() )
			if( !classpaths.contains( classesDir ) )
				classpaths.add( classesDir );

		// Jar directory
		FileUtil.listRecursiveEndsWith( container.getLibrariesFile( "jars" ), ".jar", classpaths );

		// Downloaded artifacts
		for( ArtifactDownloadReport artifact : getDownloadReports() )
		{
			if( "jar".equals( artifact.getType() ) )
			{
				File file = artifact.getLocalFile();
				if( file != null )
				{
					file = file.getAbsoluteFile();
					if( !classpaths.contains( file ) )
						classpaths.add( file );
				}
			}
		}

		// for( File f : classpaths )
		// System.out.println( f );

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
		if( has( group, name, version ) )
			return false;

		ModuleRevisionId id = ModuleRevisionId.newInstance( group, name, version );
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

	public void prune() throws SincerityException
	{
		managedArtifacts.update( getArtifacts() );
		container.updateBootstrap();
	}

	public void uninstall() throws SincerityException
	{
		getPackages().uninstall();
		managedArtifacts.clean();
		container.updateBootstrap();
	}

	private ResolveReport resolve() throws SincerityException
	{
		ivy.pushContext();
		try
		{
			ResolveReport report = ivy.resolve( moduleDescriptor, defaultResolveOptions );
			if( report.hasError() )
				throw new SincerityException( "Some dependencies could not be installed" );
			return report;

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
	}

	public void install( boolean overwrite ) throws SincerityException
	{
		int installations = container.getInstallations();
		if( installations == 0 )
			container.getSincerity().getOut().println( "Making sure all dependencies are installed and upgraded..." );

		container.initializeProgress();

		// Resolve
		if( resolve().hasChanged() )
			container.setChanged( true );

		container.updateBootstrap();

		/*
		 * for( ResolvedDependency r : getResolvedDependencies().getAll() ) {
		 * System.out.println(r.descriptor.getRevision()); }
		 */

		/*
		 * for( DependencyDescriptor dependencyDescriptor : getDescriptors() ) {
		 * if( "python".equals(
		 * dependencyDescriptor.getDependencyId().getOrganisation() ) )
		 * System.out.println( dependencyDescriptor + " " +
		 * dependencyDescriptor.getExtraAttribute( "installed" ) ); }
		 */

		if( container.hasChanged() )
		{
			// Make sure to handle package deletions first
			managedArtifacts.update( getArtifacts() );
			container.updateBootstrap();

			// Now artifacts in packages will be properly deleted
			managedArtifacts.update( getArtifacts( true, overwrite ) );

			if( container.hasFinishedInstalling() )
				printDisclaimer( container.getSincerity().getOut() );

		}
		else
		{
			// Just handle artifact installations
			managedArtifacts.update( getArtifacts( true, overwrite ) );

			if( container.hasFinishedInstalling() )
				container.getSincerity().getOut().println( "Dependencies have not changed since last install" );
		}

		container.updateBootstrap();

		container.addInstallation();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File ivyFile;

	private final ManagedArtifacts managedArtifacts;

	private final Container container;

	private final Ivy ivy;

	private final ResolveOptions defaultResolveOptions;

	private DefaultModuleDescriptor moduleDescriptor;

	private ResolvedDependencies resolvedDependencies;

	private boolean printedDisclaimer;

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

	private void printDisclaimer( PrintWriter out )
	{
		if( printedDisclaimer )
			return;

		out.println();
		out.println( "Sincerity has has downloaded software from a repository and installed in your container." );
		out.println( "However, it is up to you to ensure that it is legal for you to use the installed software." );
		out.println( "Neither the developers of Sincerity nor the maintainers of the repositories can be held" );
		out.println( "legally responsible for your usage. In particular, note that most free and open source " );
		out.println( "software licenses grant you permission to use the software, without warranty, but place" );
		out.println( "limitations on your freedom to redistribute it." );
		out.println();
		out.println( "For your convenience, an effort has been made to provide you with access to the software" );
		out.println( "licenses. However, it is up to you to ensure that these are indeed the correct licenses for" );
		out.println( "each particular software product. Neither the developers of Sincerity nor the maintainers" );
		out.println( "of the repositories can be held legally responsible for the veracity of the information" );
		out.println( "regarding the licensing of particular software products. In particular, note that software" );
		out.println( "licenses may differ per version or edition of the software product, and that some software" );
		out.println( "is available under multiple licenses." );
		out.println();
		out.println( "Use the \"sincerity dependencies:licenses\" command to see a list of all licenses, or" );
		out.println( "\"sincerity gui:gui\" for a graphical interface." );
		out.println();

		printedDisclaimer = true;
	}
}

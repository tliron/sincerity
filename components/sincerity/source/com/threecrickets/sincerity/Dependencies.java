/**
 * Copyright 2011-2014 Three Crickets LLC.
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
import java.util.Map;
import java.util.Set;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.LogOptions;
import org.apache.ivy.core.cache.ResolutionCacheManager;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.OverrideDependencyDescriptorMediator;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.plugins.matcher.ExactPatternMatcher;
import org.apache.ivy.plugins.matcher.MapMatcher;
import org.apache.ivy.plugins.matcher.PatternMatcher;
import org.apache.ivy.plugins.parser.ModuleDescriptorParser;
import org.apache.ivy.plugins.parser.ModuleDescriptorParserRegistry;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.report.XmlReportParser;
import org.apache.ivy.plugins.repository.url.URLResource;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.util.IoUtil;
import com.threecrickets.sincerity.util.StringUtil;
import com.threecrickets.sincerity.util.XmlUtil;

/**
 * Manages the dependencies of a {@link Container}, including its classpath of
 * Jars as well as other managed artifacts. With this class you can add or
 * remove dependencies to a Sincerity container.
 * <p>
 * Changes to dependencies are only actually resolved when
 * {@link #install(boolean, boolean)} is called. To access the actually resolved
 * dependencies since the last install, see {@link #getResolvedDependencies()}.
 * <p>
 * To access the Jars, use {@link #getClasspaths(boolean)}. To access the
 * artifacts, use {@link #getArtifacts()} or {@link #getPackages()}. See also
 * the {@link ManagedArtifacts} class.
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

	/**
	 * Parses the Ivy module descriptor, and loads the managed artifacts
	 * database.
	 * 
	 * @param ivyFile
	 *        The Ivy module descriptor file (usually
	 *        "/configuration/sincerity/dependencies.conf")
	 * @param artifactsFile
	 *        The managed artifacts database file (usually
	 *        "/configuration/sincerity/artifacts.conf")
	 * @param container
	 *        The container
	 * @throws SincerityException
	 *         In case of an error
	 */
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
		defaultResolveOptions.setConfs( CONFIGURATIONS );
		defaultResolveOptions.setCheckIfChanged( true );
		defaultResolveOptions.setLog( container.getSincerity().getVerbosity() >= 1 ? LogOptions.LOG_DEFAULT : LogOptions.LOG_QUIET );
	}

	//
	// Attributes
	//

	/**
	 * The container.
	 * 
	 * @return The container
	 */
	public Container getContainer()
	{
		return container;
	}

	/**
	 * The packages.
	 * 
	 * @return The packages
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Packages getPackages() throws SincerityException
	{
		return new Packages( container );
	}

	/**
	 * True if the dependency is explicit, whatever its version.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @return True if specified
	 */
	public boolean has( String group, String name )
	{
		return has( group, name, null );
	}

	/**
	 * True if the dependency is explicit with a particular version.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @param version
	 *        The dependency's version
	 * @return True if specified
	 */
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

	/**
	 * The resolved dependencies (explicit and implicit) based on the explicit
	 * dependencies, calculated in the last {@link #install(boolean, boolean)}.
	 * 
	 * @return The resolved dependencies
	 * @throws SincerityException
	 *         In case of an error
	 */
	public ResolvedDependencies getResolvedDependencies() throws SincerityException
	{
		if( resolvedDependencies == null )
			resolvedDependencies = new ResolvedDependencies( this );
		return resolvedDependencies;
	}

	/**
	 * Retrieves the set of artifacts based on currently installed packages.
	 * 
	 * @return The artifacts
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Set<Artifact> getArtifacts() throws SincerityException
	{
		return getArtifacts( false, false, false );
	}

	/**
	 * Retrieves or installs the set of artifacts based on currently installed
	 * packages.
	 * 
	 * @param install
	 *        True to allow installation of packages
	 * @param overwrite
	 *        True to force overwriting of existing files
	 * @param verify
	 *        Whether to verify the unpacking
	 * @return The artifacts
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Set<Artifact> getArtifacts( boolean install, boolean overwrite, boolean verify ) throws SincerityException
	{
		HashSet<Artifact> artifacts = new HashSet<Artifact>();
		Packages packages = getPackages();

		try
		{
			for( ArtifactDownloadReport downloadReport : getDownloadReports() )
			{
				if( downloadReport.getLocalFile() != null )
				{
					Artifact artifact = new Artifact( downloadReport.getLocalFile().getAbsoluteFile(), null, false, container );
					artifacts.add( artifact );
					managedArtifacts.add( artifact, true, null );
				}
			}

			for( Package pack : packages )
			{
				for( Artifact artifact : pack )
				{
					if( install )
						managedArtifacts.add( artifact, true, artifact.unpack( managedArtifacts, overwrite, verify ) );
					else
						managedArtifacts.add( artifact, false, null );

					artifacts.add( artifact );
				}
			}
		}
		finally
		{
			managedArtifacts.save();
		}

		if( install )
			for( Package pack : packages )
				pack.install();

		return artifacts;
	}

	/**
	 * The managed artifacts database.
	 * 
	 * @return The managed artifacts
	 */
	public ManagedArtifacts getManagedArtifacts()
	{
		return managedArtifacts;
	}

	/**
	 * The classpath based on currently installed dependencies.
	 * 
	 * @param includeSystem
	 *        True to include the system classpath
	 * @return The classpath
	 * @throws SincerityException
	 *         In case of an error
	 * @see #getClasspaths(boolean)
	 */
	public String getClasspath( boolean includeSystem ) throws SincerityException
	{
		List<File> classpaths = getClasspaths( includeSystem );
		ArrayList<String> paths = new ArrayList<String>( classpaths.size() );
		for( File file : classpaths )
			paths.add( file.getPath() );
		return StringUtil.join( paths, File.pathSeparator );
	}

	/**
	 * The classpath based on currently installed dependencies.
	 * 
	 * @param includeSystem
	 *        True to include the system classpath
	 * @return The classpath
	 * @throws SincerityException
	 *         In case of an error
	 * @see #getClasspath(boolean)
	 */
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

		// Note: if the directory doesn't exist when the class loader is first
		// initialized, then it won't be able to pick up class files added to it
		// later; so, we want to make sure it's there
		if( !classesDir.isDirectory() )
			classesDir.mkdirs();

		if( !classpaths.contains( classesDir ) )
			classpaths.add( classesDir );

		// Jar directory
		IoUtil.listRecursiveEndsWith( container.getLibrariesFile( "jars" ), ".jar", classpaths );

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

	/**
	 * The Ivy {@link DependencyDescriptor} instances for the explicit
	 * dependencies.
	 * 
	 * @return The dependency descriptors
	 */
	public DependencyDescriptor[] getDescriptors()
	{
		return moduleDescriptor.getDependencies();
	}

	/**
	 * The Ivy resolution report file, from the last {@link #resolve()}.
	 * 
	 * @return The resolution report file
	 */
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

	//
	// Operations
	//

	/**
	 * Revokes all explicit and implicit dependencies.
	 * <p>
	 * Does <i>not</i> resolve; only changes the specification.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
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

	/**
	 * Adds an explicit dependency.
	 * <p>
	 * Will not add the dependency if it is already specified.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @param version
	 *        The dependency's version
	 * @param force
	 *        Whether to force the dependency
	 * @param transitive
	 *        Whether to pull in dependencies of the dependency
	 * @return True if added
	 * @throws SincerityException
	 *         In case of an error
	 */
	public boolean add( String group, String name, String version, boolean force, boolean transitive ) throws SincerityException
	{
		if( has( group, name ) )
			return false;

		ModuleRevisionId id = ModuleRevisionId.newInstance( group, name, version );
		DefaultDependencyDescriptor dependency = new DefaultDependencyDescriptor( moduleDescriptor, id, force, false, transitive );
		dependency.addDependencyConfiguration( "default", "*" );
		moduleDescriptor.addDependency( dependency );

		save();

		return true;
	}

	/**
	 * Changes the version for an explicit dependency that has already been
	 * specified.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @param newVersion
	 *        The dependency's new version
	 * @return True if changed
	 * @throws SincerityException
	 *         In case of an error
	 */
	public boolean revise( String group, String name, String newVersion ) throws SincerityException
	{
		List<DependencyDescriptor> dependencies = new ArrayList<DependencyDescriptor>( Arrays.asList( moduleDescriptor.getDependencies() ) );
		boolean changed = false;
		for( ListIterator<DependencyDescriptor> i = dependencies.listIterator(); i.hasNext(); )
		{
			DependencyDescriptor dependency = i.next();
			ModuleRevisionId id = dependency.getDependencyRevisionId();
			if( group.equals( id.getOrganisation() ) && name.equals( id.getName() ) && !newVersion.equals( id.getRevision() ) )
			{
				i.remove();
				id = ModuleRevisionId.newInstance( id, newVersion );
				dependency = new DefaultDependencyDescriptor( moduleDescriptor, id, dependency.isForce(), dependency.isChanging(), dependency.isTransitive() );
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

	/**
	 * Revokes an explicit dependency.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @return True if removed
	 * @throws SincerityException
	 *         In case of an error
	 */
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

	/**
	 * Excludes an implicit dependency.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @return True if removed
	 * @throws SincerityException
	 *         In case of an error
	 */
	public boolean exclude( String group, String name ) throws SincerityException
	{
		for( ExcludeRule exclude : moduleDescriptor.getExcludeRules( CONFIGURATIONS ) )
		{
			ModuleId id = exclude.getId().getModuleId();
			if( group.equals( id.getOrganisation() ) && name.equals( id.getName() ) )
				return false;
		}

		ArtifactId id = new ArtifactId( new ModuleId( group, name ), PatternMatcher.ANY_EXPRESSION, PatternMatcher.ANY_EXPRESSION, PatternMatcher.ANY_EXPRESSION );
		DefaultExcludeRule exclude = new DefaultExcludeRule( id, new ExactPatternMatcher(), null );
		exclude.addConfiguration( "default" );
		moduleDescriptor.addExcludeRule( exclude );

		save();

		return true;
	}

	/**
	 * Overrides the version of an implicit dependency.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @param version
	 *        The dependency's version
	 * @return True if overridden
	 * @throws SincerityException
	 *         In case of an error
	 */
	public boolean override( String group, String name, String version ) throws SincerityException
	{
		@SuppressWarnings("unchecked")
		Map<MapMatcher, Object> rules = moduleDescriptor.getAllDependencyDescriptorMediators().getAllRules();
		for( MapMatcher matcher : rules.keySet() )
		{
			String matcherGroup = (String) matcher.getAttributes().get( "organisation" );
			String matcherName = (String) matcher.getAttributes().get( "module" );
			if( group.equals( matcherGroup ) && name.equals( matcherName ) )
				return false;
		}

		moduleDescriptor.addDependencyDescriptorMediator( new ModuleId( group, name ), new ExactPatternMatcher(), new OverrideDependencyDescriptorMediator( null, version ) );
		save();
		return true;
	}

	/**
	 * Sets the required versions of all explicit and implicit dependencies to
	 * the those that were last resolved.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void freeze() throws SincerityException
	{
		if( container.getSincerity().getVerbosity() >= 1 )
			container.getSincerity().getOut().println( "Freezing versions of all installed dependencies" );

		for( ResolvedDependency resolvedDependency : getResolvedDependencies() )
			freeze( resolvedDependency );
	}

	/**
	 * Deletes all managed artifacts which no longer have an origin.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void prune() throws SincerityException
	{
		managedArtifacts.prune( getArtifacts() );
		container.updateBootstrap();
	}

	/**
	 * Uninstalls all packages.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void uninstall() throws SincerityException
	{
		getPackages().uninstall();
		managedArtifacts.prune();
		container.updateBootstrap();
	}

	/**
	 * Installs/upgrades dependencies.
	 * 
	 * @param overwrite
	 *        True to force overwrite of existing artifact files
	 * @param verify
	 *        Whether to verify the unpacking
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void install( boolean overwrite, boolean verify ) throws SincerityException
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
			container.updateBootstrap();

			// Prune unnecessary artifacts
			managedArtifacts.prune( getArtifacts( true, overwrite, verify ) );

			if( container.hasFinishedInstalling() )
				printDisclaimer( container.getSincerity().getOut() );
		}
		else
		{
			// Just handle artifact installations
			getArtifacts( true, overwrite, verify );

			if( container.hasFinishedInstalling() )
				container.getSincerity().getOut().println( "Dependencies have not changed since last install" );
		}

		container.updateBootstrap();

		container.addInstallation();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String[] CONFIGURATIONS = new String[]
	{
		"default"
	};

	private final File ivyFile;

	private final ManagedArtifacts managedArtifacts;

	private final Container container;

	private final Ivy ivy;

	private final ResolveOptions defaultResolveOptions;

	private DefaultModuleDescriptor moduleDescriptor;

	private ResolvedDependencies resolvedDependencies;

	private boolean printedDisclaimer;

	/**
	 * Ivy resolve: checks explicit dependencies' metadata, resolves implicit
	 * dependency tree, downloads new dependencies, removes unused dependencies,
	 * creates resolution report.
	 * 
	 * @return The resolve report
	 * @throws SincerityException
	 *         In case of an error
	 */
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

	/**
	 * Sets the required versions of all explicit and implicit dependencies to
	 * the those that were last resolved.
	 * 
	 * @param resolvedDependency
	 *        The resolved dependency
	 * @throws SincerityException
	 *         In case of an error
	 */
	private void freeze( ResolvedDependency resolvedDependency ) throws SincerityException
	{
		if( container.getSincerity().getVerbosity() >= 2 )
			container.getSincerity().getOut().println( "Freezing: " + resolvedDependency );

		ModuleRevisionId id = resolvedDependency.descriptor.getModuleRevisionId();
		override( id.getOrganisation(), id.getName(), id.getRevision() );

		for( Iterator<ResolvedDependency> i = resolvedDependency.children.iterator(); i.hasNext(); )
		{
			ResolvedDependency child = i.next();
			freeze( child );
		}
	}

	/**
	 * Saves the Ivy module descriptor file (usually
	 * "/configuration/sincerity/dependencies.conf").
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
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

	/**
	 * Parses the Ivy resolution report from the last {@link #resolve()}.
	 * 
	 * @return The report parser
	 * @throws SincerityException
	 *         In case of an error
	 * @see #getResolutionReport()
	 */
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

	/**
	 * The Ivy download reports from the last {@link #resolve()}.
	 * 
	 * @return The download reports
	 * @throws SincerityException
	 *         In case of an error
	 */
	private Set<ArtifactDownloadReport> getDownloadReports() throws SincerityException
	{
		HashSet<ArtifactDownloadReport> artifacts = new HashSet<ArtifactDownloadReport>();
		XmlReportParser parser = getParsedResolutionReport();
		if( parser != null )
			artifacts.addAll( Arrays.asList( parser.getArtifactReports() ) );
		return artifacts;
	}

	/**
	 * Prints the Sincerity installation disclaimer.
	 * 
	 * @param out
	 *        The print writer
	 */
	private void printDisclaimer( PrintWriter out )
	{
		if( printedDisclaimer )
			return;

		out.println();
		out.println( "Sincerity has downloaded software from a repository and installed in your container." );
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

package com.threecrickets.sincerity.ivy.pypi;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.ivy.core.IvyPatternHelper;
import org.apache.ivy.core.cache.ArtifactOrigin;
import org.apache.ivy.core.cache.DefaultRepositoryCacheManager;
import org.apache.ivy.core.cache.RepositoryCacheManager;
import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultArtifact;
import org.apache.ivy.core.module.descriptor.DefaultDependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.report.ArtifactDownloadReport;
import org.apache.ivy.core.resolve.DownloadOptions;
import org.apache.ivy.core.resolve.ResolveData;
import org.apache.ivy.core.search.ModuleEntry;
import org.apache.ivy.core.search.OrganisationEntry;
import org.apache.ivy.core.search.RevisionEntry;
import org.apache.ivy.plugins.latest.ArtifactInfo;
import org.apache.ivy.plugins.latest.LatestStrategy;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorWriter;
import org.apache.ivy.plugins.repository.Repository;
import org.apache.ivy.plugins.repository.Resource;
import org.apache.ivy.plugins.repository.url.URLRepository;
import org.apache.ivy.plugins.repository.url.URLResource;
import org.apache.ivy.plugins.resolver.BasicResolver;
import org.apache.ivy.plugins.resolver.util.ResolvedResource;
import org.apache.ivy.util.Message;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.FileUtil;
import com.threecrickets.sincerity.ivy.SincerityRepositoryCacheManager;

public class PyPiResolver extends BasicResolver
{
	//
	// Constants
	//

	public static final String DEFAULT_ORGANIZATION = "python";

	public static final String DEFAULT_ROOT = "http://pypi.python.org/simple";

	public static final String BUILDER_ARCHIVE_TYPE = "builder-archive";

	public static final String BUILDER_EGG_TYPE = "builder-egg";

	public static final String EGG_TYPE = "python-egg";

	public static final String EGG_EXTENSION = "egg";

	public static final String EGG_FULL_EXTENSION = "." + EGG_EXTENSION;

	public static final String REQUIRES_FILENAME = "EGG-INFO/requires.txt";

	public static final String SETUP_FILENAME = "setup.py";

	//
	// Construction
	//

	public PyPiResolver()
	{
		super();
	}

	//
	// Attributes
	//

	public Repository getRepository()
	{
		return repository;
	}

	public void setRepository( Repository repository )
	{
		this.repository = repository;
	}

	public String getOrganisation()
	{
		return organisation;
	}

	public void setOrganisation( String organisation )
	{
		if( organisation == null )
			throw new NullPointerException( "organisation must not be null" );
		this.organisation = organisation;
	}

	public String getPythonVersion()
	{
		return pythonVersion;
	}

	public void setPythonVersion( String pythonVersion )
	{
		this.pythonVersion = pythonVersion;
	}

	public String getRoot()
	{
		return root;
	}

	public void setRoot( String root )
	{
		if( root == null )
			throw new NullPointerException( "root must not be null" );
		if( !root.endsWith( "/" ) )
			this.root = root + "/";
		else
			this.root = root;
	}

	//
	// DependencyResolver
	//

	@Override
	public String getTypeName()
	{
		return "pypi";
	}

	@Override
	public OrganisationEntry[] listOrganisations()
	{
		return new OrganisationEntry[]
		{
			new OrganisationEntry( this, getOrganisation() )
		};
	}

	@Override
	public ModuleEntry[] listModules( OrganisationEntry organisationEntry )
	{
		PyPi pyPi = getPyPi();
		Collection<String> moduleNames = pyPi.listModuleNames();
		ModuleEntry[] moduleEntries = new ModuleEntry[moduleNames.size()];
		int index = 0;
		for( String moduleName : moduleNames )
			moduleEntries[index++] = new ModuleEntry( new OrganisationEntry( this, getOrganisation() ), moduleName );
		return moduleEntries;
	}

	@Override
	public RevisionEntry[] listRevisions( ModuleEntry moduleEntry )
	{
		String moduleName = moduleEntry.getModule();
		Pattern pattern = PyPi.getArtifactPattern( moduleName );
		PyPi pyPi = getPyPi();
		Collection<String[]> artifacts = pyPi.listArtifacts( moduleName );
		HashSet<String> versions = new HashSet<String>();
		for( String[] artifact : artifacts )
		{
			Matcher matcher = pattern.matcher( artifact[0] );
			if( matcher.find() )
				versions.add( matcher.group( 1 ) );
		}
		RevisionEntry[] revisionEntries = new RevisionEntry[versions.size()];
		int index = 0;
		for( String version : versions )
			revisionEntries[index++] = new RevisionEntry( moduleEntry, version );
		return revisionEntries;
	}

	public void publish( Artifact artifact, File src, boolean overwrite ) throws IOException
	{
		System.out.println( "publish " + artifact );
	}

	@Override
	protected Resource getResource( String source ) throws IOException
	{
		// System.out.println( "getResource " + source );
		return getRepository().getResource( source );
	}

	@Override
	protected Collection<?> findNames( @SuppressWarnings("rawtypes") Map tokenValues, String token )
	{
		// System.out.println( "findNames " + token );
		return null;
	}

	@SuppressWarnings("unchecked")
	public ResolvedResource findIvyFileRef( DependencyDescriptor dependencyDescriptor, ResolveData resolveData )
	{
		ModuleRevisionId id = dependencyDescriptor.getDependencyRevisionId();
		if( !getOrganisation().equals( id.getOrganisation() ) )
			return null;

		boolean install = !id.getName().startsWith( PyPi.LIBRARY_PREFIX );

		// Search the PyPI repository
		PyPi pyPi = getPyPi();
		List<String[]> artifacts = pyPi.findArtifacts( id, getSettings().getVersionMatcher(), pythonVersion );
		if( !artifacts.isEmpty() )
		{
			try
			{
				// Prefer latest version
				if( artifacts.size() > 1 )
				{
					// List descriptors
					List<DefaultModuleDescriptor> artifactDescriptors = new ArrayList<DefaultModuleDescriptor>();
					for( String[] artifact : artifacts )
					{
						String version = artifact[2];
						DefaultModuleDescriptor artifactDescriptor = DefaultModuleDescriptor.newDefaultInstance( ModuleRevisionId.newInstance( id, version ) );
						artifactDescriptor.getExtraInfo().put( "artifact", artifact );
						artifactDescriptors.add( artifactDescriptor );
					}

					// Sort
					LatestStrategy latestStrategy = getSettings().getDefaultLatestStrategy();
					artifactDescriptors = (List<DefaultModuleDescriptor>) latestStrategy.sort( artifactDescriptors.toArray( new ArtifactInfo[artifactDescriptors.size()] ) );

					// Use the artifacts of latest version
					artifacts = new ArrayList<String[]>();
					String revision = null;
					for( ListIterator<DefaultModuleDescriptor> i = artifactDescriptors.listIterator( artifactDescriptors.size() ); i.hasPrevious(); )
					{
						DefaultModuleDescriptor artifactDescriptor = i.previous();
						if( revision == null || revision.equals( artifactDescriptor.getRevision() ) )
							artifacts.add( (String[]) artifactDescriptor.getExtraInfo().get( "artifact" ) );
						else
							break;
					}
				}

				// Prefer Python version-specific to generic
				if( artifacts.size() > 1 )
				{
					ArrayList<String[]> specific = new ArrayList<String[]>();
					for( String[] artifact : artifacts )
					{
						String pythonVersion = artifact[3];
						if( pythonVersion != null )
							specific.add( artifact );
					}
					if( !specific.isEmpty() )
						artifacts = specific;
				}

				// Prefer eggs to archives
				if( artifacts.size() > 1 )
				{
					ArrayList<String[]> eggs = new ArrayList<String[]>();
					for( String[] artifact : artifacts )
					{
						String extension = artifact[4];
						if( EGG_EXTENSION.equals( extension ) )
							eggs.add( artifact );
					}
					if( !eggs.isEmpty() )
						artifacts = eggs;
				}

				if( artifacts.size() > 1 )
				{
					// TODO: what does it mean when there's more than one
					// artifact at this point?
					artifacts = Collections.singletonList( artifacts.get( 0 ) );
				}

				ArrayList<DependencyArtifactDescriptor> artifactDescriptors = new ArrayList<DependencyArtifactDescriptor>();
				ArrayList<ModuleRevisionId> dependencyIds = new ArrayList<ModuleRevisionId>();
				String version = id.getRevision();
				for( String[] artifact : artifacts )
				{
					String artifactName = artifact[0];
					String artifactUri = artifact[1];
					version = artifact[2];
					String extension = artifact[4];
					if( artifactName.endsWith( "." + extension ) )
						artifactName = artifactName.substring( 0, artifactName.length() - extension.length() - 1 );
					String type = null;
					String builderType;
					if( EGG_EXTENSION.equals( extension ) )
					{
						type = BUILDER_EGG_TYPE;
						builderType = BUILDER_EGG_TYPE;
					}
					else
					{
						type = BUILDER_ARCHIVE_TYPE;
						builderType = BUILDER_ARCHIVE_TYPE;
					}

					id = ModuleRevisionId.newInstance( id, version );

					// In order to find our dependencies, we're going to have to
					// download the artifact to get the dependency list within.
					// We'll store them in the "builder" section of the cache.

					// Get the artifact
					DefaultArtifact builderArtifact = new DefaultArtifact( id, null, artifactName, builderType, extension, new URL( artifactUri ), null );
					File builderFile = getFile( builderArtifact );

					File eggFile = null;
					boolean installed = false;
					if( BUILDER_EGG_TYPE.equals( type ) )
					{
						eggFile = builderFile;

						if( install )
						{
							easyInstall( eggFile );
							installed = true;
						}
						else
						{
							type = EGG_TYPE;

							// Update sincerity.pth
							addEggToPth( eggFile );
						}
					}
					else if( BUILDER_ARCHIVE_TYPE.equals( type ) )
					{
						if( install )
						{
							// Install the archive
							setupPy( builderFile, getBuilderSourceDir( id ), null );
							installed = true;
						}
						else
						{
							// We'll try to build an egg from the archive
							eggFile = setupPy( builderFile, getBuilderSourceDir( id ), getBuilderEggDir( id ) );
							if( eggFile != null )
							{
								// Cache the built egg
								// (See findArtifactRef for its retrieval)
								URL eggUrl = eggFile.toURI().toURL();
								DefaultArtifact eggArtifact = new DefaultArtifact( id, null, artifactName, BUILDER_EGG_TYPE, EGG_EXTENSION, eggUrl, null );
								getFile( eggArtifact );

								type = EGG_TYPE;
								extension = EGG_EXTENSION;

								// Update sincerity.pth
								addEggToPth( eggFile );
							}
							else
							{
								throw new RuntimeException( "Could not make an egg out of: " + builderFile );
							}
						}
					}

					if( eggFile != null && !installed )
					{
						// Let's crack open the egg to examine its dependencies
						System.out.println( "Finding dependencies in Python egg: " + artifactName + " " + eggFile );
						dependencyIds.addAll( getDependenciesFromEgg( eggFile ) );

						// TODO: licenses!!!!
					}

					// Add artifact
					DefaultDependencyArtifactDescriptor artifactDescriptor = new DefaultDependencyArtifactDescriptor( dependencyDescriptor, artifactName, type, extension, new URL( artifactUri ), null );
					artifactDescriptors.add( artifactDescriptor );
				}

				// Every has been OK so far, but we'll need a resource for the
				// module descriptor. Of course, PyPI doesn't have one, so we'll
				// construct one and put it in the builder section of our cache.

				// The builder module descriptor
				DefaultModuleDescriptor moduleDescriptor = DefaultModuleDescriptor.newDefaultInstance( id, artifactDescriptors.toArray( new DependencyArtifactDescriptor[artifactDescriptors.size()] ) );
				for( ModuleRevisionId dependencyId : dependencyIds )
				{
					DefaultDependencyDescriptor dependency = new DefaultDependencyDescriptor( moduleDescriptor, dependencyId, false, false, true );
					dependency.addDependencyConfiguration( "default", "*" );
					moduleDescriptor.addDependency( dependency );
				}

				// Write module descriptor to file
				File descriptorFile = getBuilderIvyFile( id );
				if( descriptorFile.getParentFile() != null )
					descriptorFile.getParentFile().mkdirs();
				XmlModuleDescriptorWriter.write( moduleDescriptor, descriptorFile );
				descriptorFile.setLastModified( pyPi.getModuleLastModified( id.getName() ) );

				// An artifact for the module descriptor file
				DefaultArtifact moduleArtifact = new DefaultArtifact( id, null, descriptorFile.getName(), "ivy", "descriptor", descriptorFile.toURI().toURL(), null );

				// A resolved resource for the artifact
				// (Apparently, it has to be a UrlResource, not a FileResource)
				return new ResolvedResource( new URLResource( moduleArtifact.getUrl() ), version );

				// MetadataArtifactDownloadReport report = new
				// MetadataArtifactDownloadReport( moduleArtifact );
				// return new MDResolvedResource( new URLResource(
				// moduleArtifact.getUrl() ), id.getRevision(), new
				// ResolvedModuleRevision( this, this, moduleDescriptor, report
				// ) );
			}
			catch( Exception x )
			{
				x.printStackTrace();
			}
		}

		return null;
	}

	@Override
	protected ResolvedResource findArtifactRef( Artifact artifact, Date date )
	{
		ModuleRevisionId id = artifact.getModuleRevisionId();
		if( !getOrganisation().equals( id.getOrganisation() ) )
			return null;

		// System.out.println( "findArtifactRef " + artifact );

		// We might already have it archived
		if( EGG_TYPE.equals( artifact.getType() ) )
		{
			DefaultArtifact archivedEggArtifact = new DefaultArtifact( id, null, artifact.getName(), BUILDER_EGG_TYPE, artifact.getExt() );
			File archivedEggFile = getCachedFile( archivedEggArtifact );
			if( archivedEggFile != null )
			{
				// System.out.println( "already found: " + archivedEggFile );
				try
				{
					return new ResolvedResource( getResource( archivedEggFile.toURI().toURL().toString() ), id.getRevision() );
				}
				catch( IOException x )
				{
					x.printStackTrace();
					return null;
				}
			}
			// else
			// System.out.println( "not found" );
		}

		PyPi pyPi = getPyPi();
		String artifactUri = pyPi.getArtifactUri( id.getName(), artifact.getName() + "." + artifact.getExt() );
		if( artifactUri != null )
		{
			try
			{
				return new ResolvedResource( getResource( artifactUri ), id.getRevision() );
			}
			catch( IOException x )
			{
				x.printStackTrace();
			}
		}

		return null;
	}

	@Override
	protected long get( Resource resource, File destination ) throws IOException
	{
		// System.out.println( "get '" + resource + "' to '" + destination + "'"
		// );
		try
		{
			Message.verbose( "\t" + getName() + ": downloading " + resource.getName() );
			Message.debug( "\t\tto " + destination );
			if( destination.getParentFile() != null )
				destination.getParentFile().mkdirs();
			getRepository().get( resource.getName(), destination );
		}
		catch( Exception x )
		{
			x.printStackTrace();
		}
		return destination.length();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Repository repository = new URLRepository();

	private String organisation = DEFAULT_ORGANIZATION;

	private String root = DEFAULT_ROOT;

	private String pythonVersion = "2.5";

	private PyPi pyPi;

	private PyPi getPyPi()
	{
		if( pyPi == null )
			pyPi = new PyPi( getRoot(), getRepository() );
		return pyPi;
	}

	private File getFile( Artifact artifact )
	{
		File file = getCachedFile( artifact );
		if( file == null )
		{
			ArtifactOrigin artifactOrigin = new ArtifactOrigin( artifact, false, artifact.getUrl().toString() );
			ArtifactDownloadReport report = download( artifactOrigin, new DownloadOptions() );
			file = report.getLocalFile();
		}
		return file;
	}

	private File getCachedFile( Artifact artifact )
	{
		RepositoryCacheManager cacheManager = getRepositoryCacheManager();
		if( cacheManager instanceof DefaultRepositoryCacheManager )
		{
			File file = ( (DefaultRepositoryCacheManager) cacheManager ).getArchiveFileInCache( artifact );
			if( file.exists() )
				return file;
		}
		return null;
	}

	private File getPthFile()
	{
		RepositoryCacheManager repositoryCacheManager = getRepositoryCacheManager();
		if( repositoryCacheManager instanceof SincerityRepositoryCacheManager )
		{
			SincerityRepositoryCacheManager sincerityRepositoryCacheManager = (SincerityRepositoryCacheManager) repositoryCacheManager;
			return new File( sincerityRepositoryCacheManager.getRepositoryCacheRoot(), "libraries/python/Lib/site-packages/sincerity.pth" );
		}
		throw new RuntimeException( "PyPiResolver requires a SincerityRepositoryCacheManager to be configured" );
	}

	private File getBuilderIvyFile( ModuleRevisionId id )
	{
		RepositoryCacheManager repositoryCacheManager = getRepositoryCacheManager();
		if( repositoryCacheManager instanceof SincerityRepositoryCacheManager )
		{
			SincerityRepositoryCacheManager sincerityRepositoryCacheManager = (SincerityRepositoryCacheManager) repositoryCacheManager;
			return new File( sincerityRepositoryCacheManager.getRepositoryCacheRoot(), IvyPatternHelper.substitute( sincerityRepositoryCacheManager.getBuilderIvyPattern(), id ) );
		}
		throw new RuntimeException( "PyPiResolver requires a SincerityRepositoryCacheManager to be configured" );
	}

	private File getBuilderEggDir( ModuleRevisionId id )
	{
		RepositoryCacheManager repositoryCacheManager = getRepositoryCacheManager();
		if( repositoryCacheManager instanceof SincerityRepositoryCacheManager )
		{
			SincerityRepositoryCacheManager sincerityRepositoryCacheManager = (SincerityRepositoryCacheManager) repositoryCacheManager;
			return new File( sincerityRepositoryCacheManager.getRepositoryCacheRoot(), IvyPatternHelper.substitute( sincerityRepositoryCacheManager.getBuilderEggDirPattern(), id ) );
		}
		throw new RuntimeException( "PyPiResolver requires a SincerityRepositoryCacheManager to be configured" );
	}

	private File getBuilderSourceDir( ModuleRevisionId id )
	{
		RepositoryCacheManager repositoryCacheManager = getRepositoryCacheManager();
		if( repositoryCacheManager instanceof SincerityRepositoryCacheManager )
		{
			SincerityRepositoryCacheManager sincerityRepositoryCacheManager = (SincerityRepositoryCacheManager) repositoryCacheManager;
			return new File( sincerityRepositoryCacheManager.getRepositoryCacheRoot(), IvyPatternHelper.substitute( sincerityRepositoryCacheManager.getBuilderSourceDirPattern(), id ) );
		}
		throw new RuntimeException( "PyPiResolver requires a SincerityRepositoryCacheManager to be configured" );
	}

	private static void easyInstall( File eggFile ) throws IOException
	{
		Sincerity sincerity = Sincerity.getCurrent();
		if( sincerity != null )
		{
			try
			{
				sincerity.run( "python" + Command.PLUGIN_COMMAND_SEPARATOR + "easy_install", eggFile.getPath() );
			}
			catch( SincerityException x )
			{
				x.printStackTrace();
			}
		}
	}

	private static File setupPy( File archiveFile, File sourceDir, File eggsDir ) throws IOException
	{
		// Do we have a cached built egg?
		if( ( eggsDir != null ) && eggsDir.isDirectory() )
			for( File file : eggsDir.listFiles() )
				return file;

		// Unpack only if we haven't already unpacked into the cache
		if( !sourceDir.isDirectory() )
			FileUtil.unpack( archiveFile, sourceDir, sourceDir );

		return setupPy( sourceDir, eggsDir );
	}

	private static File setupPy( File sourceDir, File eggsDir ) throws IOException
	{
		// Find setup.py
		File setupFile = null;
		if( sourceDir.isDirectory() )
		{
			for( File dir : sourceDir.listFiles() )
			{
				if( dir.isDirectory() )
				{
					for( File file : dir.listFiles() )
					{
						if( SETUP_FILENAME.equals( file.getName() ) )
						{
							setupFile = file;
							break;
						}
					}
				}
				if( setupFile != null )
					break;
			}
		}

		if( setupFile != null )
		{
			Sincerity sincerity = Sincerity.getCurrent();
			if( sincerity != null )
			{
				try
				{
					// Notes:
					//
					// 1. setup.py often expects to be in the current
					// directory
					//
					// 2. bdist_egg is not included in distutils, but by
					// importing setuptools we let it install its extensions
					// so that distutils can use them

					sincerity.run( "python" + Command.PLUGIN_COMMAND_SEPARATOR + "python", "-c", "import os, setuptools; os.chdir('" + setupFile.getParent().replace( "'", "\\'" ) + "');" );

					if( eggsDir != null )
						sincerity.run( "python" + Command.PLUGIN_COMMAND_SEPARATOR + "python", setupFile.getPath(), "bdist_egg", "--dist-dir=" + eggsDir.getPath() );
					else
						sincerity.run( "python" + Command.PLUGIN_COMMAND_SEPARATOR + "python", setupFile.getPath(), "install", "--install-scripts=" + sincerity.getContainer().getExecutablesFile() );
				}
				catch( SincerityException x )
				{
					x.printStackTrace();
				}
			}

			// Return the built egg
			if( ( eggsDir != null ) && eggsDir.isDirectory() )
				for( File file : eggsDir.listFiles() )
					if( file.getName().endsWith( EGG_FULL_EXTENSION ) )
						return file;
		}

		return null;
	}

	private List<ModuleRevisionId> getDependenciesFromEgg( File eggFile ) throws IOException
	{
		ArrayList<ModuleRevisionId> dependencyIds = new ArrayList<ModuleRevisionId>();
		ZipFile zip = new ZipFile( eggFile );
		ZipEntry requiresEntry = zip.getEntry( REQUIRES_FILENAME );
		if( requiresEntry != null )
		{
			List<String> lines = FileUtil.readLines( zip.getInputStream( requiresEntry ) );
			for( String line : lines )
			{
				String name = null, dependencyVersion = null;
				if( line.contains( "==" ) )
				{
					String[] split = line.split( "==", 2 );
					name = split[0];
					dependencyVersion = split[1];
				}
				else if( line.contains( ">=" ) )
				{
					// See:
					// https://ant.apache.org/ivy/history/latest-milestone/ivyfile/dependency.html#revision
					String[] split = line.split( ">=", 2 );
					name = split[0];
					dependencyVersion = "[" + split[1] + ",)";
				}
				if( name != null )
				{
					name = PyPi.LIBRARY_PREFIX + name;
					ModuleRevisionId dependencyId = ModuleRevisionId.newInstance( getOrganisation(), name, dependencyVersion );
					dependencyIds.add( dependencyId );
				}
			}
		}
		return dependencyIds;
	}

	private void addEggToPth( File eggFile ) throws IOException
	{
		String pth = "./" + eggFile.getName();
		File pthFile = getPthFile();
		List<String> pths = FileUtil.readLines( pthFile );
		if( !pths.contains( pths ) )
		{
			pths.add( pth );
			FileUtil.writeLines( pthFile, pths );
		}
	}
}

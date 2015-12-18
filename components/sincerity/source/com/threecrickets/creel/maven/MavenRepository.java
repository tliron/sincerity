package com.threecrickets.creel.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.threecrickets.creel.Module;
import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.ModuleSpecification;
import com.threecrickets.creel.Repository;
import com.threecrickets.creel.Rule;
import com.threecrickets.creel.event.Notifier;
import com.threecrickets.creel.exception.IncompatiblePlatformException;
import com.threecrickets.creel.internal.ConfigHelper;
import com.threecrickets.creel.maven.internal.InvalidSignatureException;
import com.threecrickets.creel.maven.internal.MetaData;
import com.threecrickets.creel.maven.internal.POM;
import com.threecrickets.creel.maven.internal.Signature;
import com.threecrickets.creel.maven.internal.SpecificationOption;
import com.threecrickets.sincerity.util.IoUtil;

/**
 * Dependency management support for
 * <a href="https://maven.apache.org/">Maven</a> m2 (also known as "ibiblio")
 * repositories.
 * <p>
 * Supports reading the repository URL structure, retrieving and parsing ".pom"
 * and "maven-metadata.xml" data, interpreting module identifiers
 * (group/name/version), applying version ranges, downloading ".jar" files, and
 * validating against signatures in ".sha1" or ".md5" files.
 * <p>
 * For convenience, we also support the
 * <a href="http://ant.apache.org/ivy/">Ivy</a>-style "+" version range, even
 * though it is not part of the Maven standard.
 * <p>
 * Additionally, pattern matching ("*", "?") is supported, as well as exclusions
 * ("!").
 * 
 * @author Tal Liron
 */
public class MavenRepository extends Repository
{
	//
	// Static operations
	//

	public static MavenRepository cast( Object object )
	{
		if( object == null )
			throw new NullPointerException();
		if( !( object instanceof MavenRepository ) )
			throw new IncompatiblePlatformException();
		return (MavenRepository) object;
	}

	//
	// Construction
	//

	public MavenRepository( String id, boolean all, int parallelism, URL url, boolean checkSignatures, boolean allowMd5 )
	{
		super( id, all, parallelism );
		this.url = url;
		this.checkSignatures = checkSignatures;
		this.allowMd5 = allowMd5;
	}

	public MavenRepository( Map<String, ?> config )
	{
		super( config );
		ConfigHelper configHelper = new ConfigHelper( config );
		try
		{
			url = new URL( configHelper.getString( "url" ) );
		}
		catch( MalformedURLException x )
		{
			throw new RuntimeException( x );
		}
		checkSignatures = configHelper.getBoolean( "checkSignatures", true );
		allowMd5 = configHelper.getBoolean( "allowMd5" );
	}

	//
	// Attributes
	//

	public URL getUrl()
	{
		return url;
	}

	public boolean isCheckSignatures()
	{
		return checkSignatures;
	}

	public boolean isAllowMd5()
	{
		return allowMd5;
	}

	public File getFile( MavenModuleIdentifier moduleIdentifier, String extension, File directory )
	{
		File file = directory;
		file = new File( file, moduleIdentifier.getGroup() );
		file = new File( file, moduleIdentifier.getName() );
		file = new File( file, moduleIdentifier.getVersion() );
		file = new File( file, moduleIdentifier.getName() + '.' + extension );
		return file;
	}

	public URL getUrl( MavenModuleIdentifier moduleIdentifier, String extension )
	{
		StringBuilder url = new StringBuilder( getUrl().toString() );

		for( String part : moduleIdentifier.getGroup().split( "\\." ) )
		{
			url.append( '/' );
			url.append( part );
		}

		url.append( '/' );
		url.append( moduleIdentifier.getName() );
		url.append( '/' );
		url.append( moduleIdentifier.getVersion() );
		url.append( '/' );
		url.append( moduleIdentifier.getName() );
		url.append( '-' );
		url.append( moduleIdentifier.getVersion() );
		url.append( '.' );
		url.append( extension );

		try
		{
			return new URL( url.toString() ).toURI().normalize().toURL();
		}
		catch( MalformedURLException x )
		{
			throw new RuntimeException( x );
		}
		catch( URISyntaxException x )
		{
			throw new RuntimeException( x );
		}
	}

	public URL getMetaDataUrl( String group, String name )
	{
		StringBuilder url = new StringBuilder( getUrl().toString() );

		for( String part : group.split( "\\." ) )
		{
			url.append( '/' );
			url.append( part );
		}

		url.append( '/' );
		url.append( name );
		url.append( "/maven-metadata.xml" );

		try
		{
			return new URL( url.toString() ).toURI().normalize().toURL();
		}
		catch( MalformedURLException x )
		{
			throw new RuntimeException( x );
		}
		catch( URISyntaxException x )
		{
			throw new RuntimeException( x );
		}
	}

	public POM getPom( MavenModuleIdentifier moduleIdentifier, Notifier notifier )
	{
		if( notifier == null )
			notifier = new Notifier();

		// TODO: cache POMs
		URL url = getUrl( moduleIdentifier, "pom" );
		try
		{
			Signature signature = isCheckSignatures() ? new Signature( url, isAllowMd5() ) : null;
			POM pom = new POM( url, signature );
			if( !moduleIdentifier.equals( pom.getModuleIdentifier( this ) ) )
			{
				notifier.error( "Invalid POM: " + url );
				return null;
			}
			return pom;
		}
		catch( FileNotFoundException x )
		{
			notifier.debug( "No POM: " + url );
			return null;
		}
		catch( InvalidSignatureException x )
		{
			notifier.error( "Invalid signature for POM: " + url );
			return null;
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}
	}

	public MetaData getMetaData( String group, String name, Notifier notifier )
	{
		if( notifier == null )
			notifier = new Notifier();

		// TODO: cache metadata!
		URL url = getMetaDataUrl( group, name );
		try
		{
			Signature signature = isCheckSignatures() ? new Signature( url, isAllowMd5() ) : null;
			MetaData metadata = new MetaData( url, signature );
			if( !group.equals( metadata.getGroupId() ) || !name.equals( metadata.getArtifactId() ) )
			{
				notifier.error( "Invalid metadata: " + url );
				return null;
			}
			return metadata;
		}
		catch( FileNotFoundException x )
		{
			notifier.debug( "No metadata: " + url );
			return null;
		}
		catch( InvalidSignatureException x )
		{
			notifier.error( "Invalid signature for metadata: " + url );
			return null;
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}
	}

	//
	// Repository
	//

	@Override
	public boolean hasModule( ModuleIdentifier moduleIdentifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = MavenModuleIdentifier.cast( moduleIdentifier );
		URL url = getUrl( mavenModuleIdentifier, "pom" );
		return IoUtil.isUrlValid( url );
	}

	@Override
	public Module getModule( ModuleIdentifier moduleIdentifier, Notifier notifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = MavenModuleIdentifier.cast( moduleIdentifier );
		if( notifier == null )
			notifier = new Notifier();

		POM pom = getPom( mavenModuleIdentifier, notifier );
		if( pom == null )
			return null;

		Module module = new Module( false, moduleIdentifier, null );
		for( MavenModuleSpecification moduleSpecification : pom.getDependencyModuleSpecifications() )
		{
			Module dependencyModule = new Module( false, null, moduleSpecification );
			dependencyModule.addSupplicant( module );
			module.addDependency( dependencyModule );
		}
		return module;
	}

	@Override
	public Iterable<ModuleIdentifier> getAllowedModuleIdentifiers( ModuleSpecification moduleSpecification, Notifier notifier )
	{
		MavenModuleSpecification mavenModuleSpecification = MavenModuleSpecification.cast( moduleSpecification );
		if( notifier == null )
			notifier = new Notifier();

		Set<ModuleIdentifier> potentialModuleIdentifiers = new LinkedHashSet<ModuleIdentifier>();
		for( SpecificationOption option : mavenModuleSpecification.getOptions() )
		{
			if( option.getParsedVersionSpecfication( false ).isTrivial() )
			{
				// When the version is trivial, we can skip the metadata and
				// just check directly against the POM
				MavenModuleIdentifier moduleIdentifier = option.toModuleIdentifier( false, this );
				if( hasModule( moduleIdentifier ) )
					potentialModuleIdentifiers.add( moduleIdentifier );
			}
			else
			{
				MetaData metadata = getMetaData( option.getGroup(), option.getName(), notifier );
				if( metadata != null )
					for( MavenModuleIdentifier moduleIdentifier : metadata.getModuleIdentifiers( this ) )
						potentialModuleIdentifiers.add( moduleIdentifier );
			}
		}

		return moduleSpecification.filterAllowedModuleIdentifiers( potentialModuleIdentifiers );
	}

	@Override
	public void installModule( ModuleIdentifier moduleIdentifier, File directory, boolean overwrite, Notifier notifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = MavenModuleIdentifier.cast( moduleIdentifier );
		if( notifier == null )
			notifier = new Notifier();

		URL url = getUrl( mavenModuleIdentifier, "jar" );
		File file = getFile( mavenModuleIdentifier, "jar", directory );

		boolean downloading = overwrite || !file.exists();

		String id;
		if( downloading )
			id = notifier.begin( "Downloading from " + url, 0.0 );
		else
			id = notifier.begin( "Validating " + file );

		Signature signature;
		try
		{
			signature = new Signature( url, allowMd5 );
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}

		if( downloading )
		{
			file.getParentFile().mkdirs();
			try
			{
				IoUtil.copy( url, file );
			}
			catch( IOException x )
			{
				throw new RuntimeException( x );
			}
			for( int i = 0; i <= 100; i += 10 )
			{
				notifier.update( id, i / 100.0 );
				// Sincerity.JVM.sleep(100) // :)
			}
			notifier.update( id, "Validating " + file );
		}

		// Sincerity.JVM.sleep(300) // :)
		if( signature.equals( file ) )
		{
			if( downloading )
				notifier.end( id, "Downloaded " + file );
			else
				notifier.end( id, "Validated " + file );
		}
		else
		{
			notifier.fail( id, "File does not match signature: " + file );
			file.delete();
			// throw ':('
		}
	}

	@Override
	public String applyModuleRule( Module module, Rule rule, Notifier notifier )
	{
		if( notifier == null )
			notifier = new Notifier();

		return null;
	}

	//
	// Cloneable
	//

	@Override
	public MavenRepository clone()
	{
		return new MavenRepository( getId(), isAll(), getParallelism(), getUrl(), isCheckSignatures(), isAllowMd5() );
	}

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( !super.equals( object ) )
			return false;
		MavenRepository mavenRepository = (MavenRepository) object;
		return getUrl().equals( mavenRepository.getUrl() ) && ( isCheckSignatures() == mavenRepository.isCheckSignatures() ) && ( isAllowMd5() == mavenRepository.isAllowMd5() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( super.hashCode(), getUrl(), isCheckSignatures(), isAllowMd5() );
	}

	@Override
	public String toString()
	{
		return "id=" + getId() + ", url=maven:" + getUrl() + ", checkSignatures=" + isCheckSignatures() + ", allowMd5=" + isAllowMd5();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL url;

	private final boolean checkSignatures;

	private final boolean allowMd5;
}

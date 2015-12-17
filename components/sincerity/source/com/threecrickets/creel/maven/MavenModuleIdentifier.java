package com.threecrickets.creel.maven;

import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.exception.IncompatibleIdentifierException;
import com.threecrickets.creel.exception.IncompatiblePlatformException;
import com.threecrickets.creel.maven.internal.Version;

public class MavenModuleIdentifier extends ModuleIdentifier
{
	//
	// Static operations
	//

	public static MavenModuleIdentifier cast( ModuleIdentifier moduleIdentifier )
	{
		if( moduleIdentifier == null )
			throw new NullPointerException();
		if( !( moduleIdentifier instanceof MavenModuleIdentifier ) )
			throw new IncompatiblePlatformException();
		return (MavenModuleIdentifier) moduleIdentifier;
	}

	//
	// Construction
	//

	public MavenModuleIdentifier( MavenRepository repository, String group, String name, String version )
	{
		super( repository );
		this.group = group == null ? "" : group.trim();
		this.name = name == null ? "" : name.trim();
		this.version = version == null ? "" : version.trim();
	}

	//
	// Attributes
	//

	public String getGroup()
	{
		return group;
	}

	public String getName()
	{
		return name;
	}

	public String getVersion()
	{
		return version;
	}

	public Version getParsedVersion()
	{
		if( parsedVersion == null )
			parsedVersion = new Version( version );
		return parsedVersion;
	}

	//
	// Comparable
	//

	public int compareTo( ModuleIdentifier moduleIdentifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = cast( moduleIdentifier );
		if( group.equals( mavenModuleIdentifier.group ) && name.equals( mavenModuleIdentifier.name ) )
			return getParsedVersion().compareTo( mavenModuleIdentifier.getParsedVersion() );
		throw new IncompatibleIdentifierException();
	}

	//
	// Cloneable
	//

	@Override
	public MavenModuleIdentifier clone()
	{
		return new MavenModuleIdentifier( (MavenRepository) getRepository(), group, name, version );
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return "maven:" + group + ":" + name + ":" + version;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String group;

	private final String name;

	private final String version;

	private volatile Version parsedVersion;
}

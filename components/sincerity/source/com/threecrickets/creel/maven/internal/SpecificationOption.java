package com.threecrickets.creel.maven.internal;

import java.util.regex.Pattern;

import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.sincerity.util.GlobUtil;

public class SpecificationOption
{
	//
	// Construction
	//

	public SpecificationOption( String group, String name, String version )
	{
		group = group == null ? "" : group.trim();
		name = name == null ? "" : name.trim();
		version = version == null ? "" : version.trim();
		this.group = group.isEmpty() ? "*" : group;
		this.name = name.isEmpty() ? "*" : name;
		this.version = version;
		exclude = !version.isEmpty() && ( version.charAt( 0 ) == '!' );
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

	public boolean isExclude()
	{
		return exclude;
	}

	public Pattern getGroupPattern()
	{
		if( groupPattern == null )
			groupPattern = GlobUtil.toPattern( group );
		return groupPattern;
	}

	public Pattern getNamePattern()
	{
		if( namePattern == null )
			namePattern = GlobUtil.toPattern( name );
		return namePattern;
	}

	public boolean matches( MavenModuleIdentifier moduleIdentifier )
	{
		return matches( moduleIdentifier.getGroup(), moduleIdentifier.getName() );
	}

	public boolean matches( String group, String name )
	{
		return getGroupPattern().matcher( group ).matches() && getNamePattern().matcher( name ).matches();
	}

	public VersionSpecification getParsedVersionSpecfication( boolean strict )
	{
		if( parsedVersionSpecification == null )
		{
			String version = exclude ? this.version.substring( 1 ) : this.version;
			parsedVersionSpecification = new VersionSpecification( version, strict );
		}
		return parsedVersionSpecification;
	}

	public MavenModuleIdentifier toModuleIdentifier( boolean strict, MavenRepository repository )
	{
		if( getParsedVersionSpecfication( strict ).isTrivial() )
			return new MavenModuleIdentifier( repository, group, name, version );
		return null;
	}

	public boolean equals( SpecificationOption option )
	{
		return group.equals( option.group ) && name.equals( option.name ) && version.equals( option.version );
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return group + ":" + name + ":" + version;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String group;

	private final String name;

	private final String version;

	private final boolean exclude;

	private volatile Pattern groupPattern;

	private volatile Pattern namePattern;

	private volatile VersionSpecification parsedVersionSpecification;
}

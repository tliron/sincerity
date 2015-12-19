/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.maven.internal;

import java.util.Objects;
import java.util.regex.Pattern;

import com.threecrickets.creel.maven.MavenModuleIdentifier;
import com.threecrickets.creel.maven.MavenRepository;
import com.threecrickets.creel.util.GlobUtil;

/**
 * @author Tal Liron
 */
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
			groupPattern = GlobUtil.toPattern( getGroup() );
		return groupPattern;
	}

	public Pattern getNamePattern()
	{
		if( namePattern == null )
			namePattern = GlobUtil.toPattern( getName() );
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
			String version = isExclude() ? getVersion().substring( 1 ) : getVersion();
			parsedVersionSpecification = new VersionSpecification( version, strict );
		}
		return parsedVersionSpecification;
	}

	public MavenModuleIdentifier toModuleIdentifier( boolean strict, MavenRepository repository )
	{
		if( getParsedVersionSpecfication( strict ).isTrivial() )
			return new MavenModuleIdentifier( repository, getGroup(), getName(), getVersion() );
		return null;
	}

	//
	// Object
	//

	@Override
	public boolean equals( Object object )
	{
		if( ( object == null ) || ( getClass() != object.getClass() ) )
			return false;
		SpecificationOption option = (SpecificationOption) object;
		return getGroup().equals( option.getGroup() ) && getName().equals( option.getName() ) && getVersion().equals( option.getVersion() );
	}

	@Override
	public int hashCode()
	{
		return Objects.hash( getGroup(), getName(), getVersion() );
	}

	@Override
	public String toString()
	{
		return getGroup() + ":" + getName() + ":" + getVersion();
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

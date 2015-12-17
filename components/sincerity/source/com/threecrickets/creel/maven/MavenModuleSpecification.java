package com.threecrickets.creel.maven;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.threecrickets.creel.ModuleIdentifier;
import com.threecrickets.creel.ModuleSpecification;
import com.threecrickets.creel.exception.IncompatiblePlatformException;
import com.threecrickets.creel.internal.ConfigHelper;
import com.threecrickets.creel.maven.internal.SpecificationOption;

/**
 * Maven specification with support for version ranges.
 * <p>
 * Note that a Maven version range can in fact contain several ranges, in which
 * case they match via a logical or. For example. "(,1.1),(1.1,)" means that
 * everything except "1.1" will match.
 * <p>
 * Likewise, you may have a specification with more than one option, which will
 * also match via a logical or, <i>unless</i> the option has a version beginning
 * with a "!". That signifies an exclusion, which will always take precedence.
 * For example, "!1.1" will explicitly reject "1.1", even if "1.1" is matched by
 * other options.
 * 
 * @author Tal Liron
 */
public class MavenModuleSpecification extends ModuleSpecification
{
	//
	// Static operations
	//

	public static MavenModuleSpecification cast( ModuleSpecification moduleSpecification )
	{
		if( moduleSpecification == null )
			throw new NullPointerException();
		if( !( moduleSpecification instanceof MavenModuleSpecification ) )
			throw new IncompatiblePlatformException();
		return (MavenModuleSpecification) moduleSpecification;
	}

	//
	// Construction
	//

	public MavenModuleSpecification( String group, String name, String version, boolean strict )
	{
		options.add( new SpecificationOption( group, name, version ) );
		this.strict = strict;
	}

	public MavenModuleSpecification( Iterable<SpecificationOption> options, boolean strict )
	{
		for( SpecificationOption option : options )
			this.options.add( option );
		this.strict = strict;
	}

	public MavenModuleSpecification( Map<String, ?> config )
	{
		ConfigHelper configHelper = new ConfigHelper( config );
		String group = configHelper.getString( "group" );
		String name = configHelper.getString( "name" );
		String version = configHelper.getString( "version" );
		if( ( group == null ) || ( name == null ) )
			throw new RuntimeException();
		options.add( new SpecificationOption( group, name, version ) );
		strict = configHelper.getBoolean( "strict" );
	}

	//
	// Attributes
	//

	public Iterable<SpecificationOption> getOptions()
	{
		return Collections.unmodifiableCollection( options );
	}

	public boolean isStrict()
	{
		return strict;
	}

	//
	// ModuleSpecification
	//

	@Override
	public boolean equals( ModuleSpecification moduleSpecification )
	{
		if( !( moduleSpecification instanceof MavenModuleSpecification ) )
			return false;
		MavenModuleSpecification mavenModuleSpecification = (MavenModuleSpecification) moduleSpecification;

		if( options.size() != mavenModuleSpecification.options.size() )
			return false;

		for( Iterator<SpecificationOption> i1 = options.iterator(), i2 = mavenModuleSpecification.options.iterator(); i1.hasNext(); )
		{
			SpecificationOption o1 = i1.next(), o2 = i2.next();
			if( !o1.equals( o2 ) )
				return false;
		}

		return true;
	}

	@Override
	public boolean allowsModuleIdentifier( ModuleIdentifier moduleIdentifier )
	{
		MavenModuleIdentifier mavenModuleIdentifier = MavenModuleIdentifier.cast( moduleIdentifier );

		boolean allowed = false;

		for( SpecificationOption option : options )
		{
			if( !option.matches( mavenModuleIdentifier ) )
				continue;

			if( allowed && !option.isExclude() )
				// Logical or: we're already in, no need to check another
				// option, *unless* it's an exclusion
				continue;

			allowed = option.getParsedVersionSpecfication( strict ).allows( mavenModuleIdentifier.getParsedVersion() );

			if( option.isExclude() && allowed )
				return false; // exclusions take precedence
		}

		return allowed;
	}

	//
	// Cloneable
	//

	@Override
	public MavenModuleSpecification clone()
	{
		return new MavenModuleSpecification( options, strict );
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		String r = "maven:{";
		for( Iterator<SpecificationOption> i = options.iterator(); i.hasNext(); )
		{
			SpecificationOption option = i.next();
			r += option;
			if( i.hasNext() )
				r += "|";
		}
		r += "}";
		return r;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Collection<SpecificationOption> options = new ArrayList<SpecificationOption>();

	private final boolean strict;
}

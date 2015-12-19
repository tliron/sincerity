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

package com.threecrickets.creel.util;

import java.util.Map;

import com.threecrickets.creel.Manager;
import com.threecrickets.creel.ModuleSpecification;
import com.threecrickets.creel.Repository;

/**
 * @author Tal Liron
 */
public class ConfigHelper
{
	//
	// Static operations
	//

	public static Repository newRepository( String type, Map<String, ?> config )
	{
		return newInstance( type, "Repository", config );
	}

	public static ModuleSpecification newModuleSpecification( String type, Map<String, ?> config )
	{
		return newInstance( type, "ModuleSpecification", config );
	}

	public static <T> T newInstance( String type, String suffix, Map<String, ?> config )
	{
		String packageName = Manager.class.getPackage().getName() + "." + type;
		String className = type.substring( 0, 1 ).toUpperCase() + type.substring( 1 ) + suffix;
		return ClassUtil.newInstance( packageName + "." + className, config );
	}

	//
	// Construction
	//

	public ConfigHelper( Map<String, ?> config )
	{
		this.config = config;
	}

	//
	// Attributes
	//

	public String getString( String key )
	{
		return getString( key, null );
	}

	public String getString( String key, String defaultValue )
	{
		Object value = config.get( key );
		return value != null ? value.toString() : defaultValue;
	}

	public int getInt( String key )
	{
		return getInt( key, 0 );
	}

	public int getInt( String key, int defaultValue )
	{
		Object value = config.get( key );
		if( value instanceof Number )
			return ( (Number) value ).intValue();
		return value != null ? Integer.parseInt( value.toString() ) : defaultValue;
	}

	public boolean getBoolean( String key )
	{
		return getBoolean( key, false );
	}

	public boolean getBoolean( String key, boolean defaultValue )
	{
		Object value = config.get( key );
		if( value instanceof Boolean )
			return (Boolean) value;
		if( value instanceof Number )
			return ( (Number) value ).intValue() != 0;
		return value != null ? Boolean.parseBoolean( value.toString() ) : defaultValue;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Map<String, ?> config;
}

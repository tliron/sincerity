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

/**
 * @author Tal Liron
 */
public class ConfigHelper
{
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

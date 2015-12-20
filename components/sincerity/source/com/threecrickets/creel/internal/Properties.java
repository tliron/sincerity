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

package com.threecrickets.creel.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author Tal Liron
 */
public class Properties extends java.util.Properties
{
	//
	// Construction
	//

	public Properties( File file ) throws IOException
	{
		load( new BufferedReader( new FileReader( file ) ) );
	}

	//
	// Attributes
	//

	public Collection<Map<String, ?>> getExplicitModuleConfigs()
	{
		return getConfigs( "module" );
	}

	public Collection<Map<String, ?>> getRepositoryConfigs()
	{
		return getConfigs( "repository" );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private Collection<Map<String, ?>> getConfigs( String type )
	{
		SortedMap<Integer, Map<String, ?>> configs = new TreeMap<Integer, Map<String, ?>>();

		for( Object key : keySet() )
		{
			String name = key.toString();
			if( name.startsWith( type + '.' ) )
			{
				String[] parts = name.split( "\\." );

				if( parts.length != 3 )
					continue;

				int index;
				try
				{
					index = Integer.parseInt( parts[1] );
				}
				catch( NumberFormatException x )
				{
					continue;
				}
				String attribute = parts[2];

				@SuppressWarnings("unchecked")
				Map<String, Object> config = (Map<String, Object>) configs.get( index );
				if( config == null )
				{
					config = new HashMap<String, Object>();
					configs.put( index, config );
				}

				config.put( attribute, get( key ) );
			}
		}

		return Collections.unmodifiableCollection( configs.values() );
	}
}

/**
 * Copyright 2011-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.BaseConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.helpers.NameUtil;

import com.threecrickets.sincerity.Sincerity;

/**
 * A configuration that is prepared via API, rather than loading it from file.
 * 
 * @author Tal Liron
 */
public class ProgrammableLog4jConfiguration extends BaseConfiguration
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        The configuration name
	 */
	public ProgrammableLog4jConfiguration( String name )
	{
		super();
		setName( name );
	}

	//
	// Operations
	//

	/**
	 * Uses this configuration.
	 */
	public void use()
	{
		Sincerity sincerity = Sincerity.getCurrent();
		if( sincerity.getVerbosity() >= 2 )
			sincerity.getOut().println( "Using log4j configuration: " + getName() );

		new ProgrammableLog4jConfigurationFactory( this ).use();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected void doConfigure()
	{
		// Unfortunately, BaseConfiguration uses a private "root" field, which
		// we cannot easily affect, and that field is used in its private
		// setParents() method. Thus, we will need to calculate the parenthood
		// ourselves, using our own known root.

		Sincerity sincerity = Sincerity.getCurrent();

		if( sincerity.getVerbosity() >= 2 )
			sincerity.getOut().println( "Loggers:" );

		LoggerConfig root = getLogger( LogManager.ROOT_LOGGER_NAME );

		for( Map.Entry<String, LoggerConfig> entry : sortedMap( getLoggers() ).entrySet() )
		{
			String name = entry.getKey();
			LoggerConfig logger = entry.getValue();

			logger.setParent( null );
			if( !LogManager.ROOT_LOGGER_NAME.equals( name ) )
			{
				name = NameUtil.getSubName( name );
				while( name != null )
				{
					LoggerConfig parent = getLogger( name );
					if( parent != null )
					{
						logger.setParent( parent );
						break;
					}
					name = NameUtil.getSubName( name );
				}

				if( logger.getParent() == null )
					logger.setParent( root );
			}

			if( sincerity.getVerbosity() >= 2 )
			{
				sincerity.getOut().println(
					"  \"" + logger.getName() + "\" (" + logger.getLevel() + ( logger.isAdditive() ? ") +" : ") " ) + ( logger.getParent() == null ? "" : "> \"" + logger.getParent().getName() + "\"" ) );

				for( Appender appender : logger.getAppenders().values() )
					sincerity.getOut().println( "    -> \"" + appender.getName() + "\"" );
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Sort a map by the natural order of its keys.
	 * 
	 * @param <K>
	 *        The map key class
	 * @param <V>
	 *        The map value class
	 * @param map
	 *        The map
	 * @return The sorted map
	 */
	private static <K extends Comparable<? super K>, V> LinkedHashMap<K, V> sortedMap( Map<K, V> map )
	{
		LinkedList<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
		Collections.sort( list, new Comparator<Map.Entry<K, V>>()
		{
			public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
			{
				try
				{
					return ( o1.getKey() ).compareTo( o2.getKey() );
				}
				catch( ClassCastException x )
				{
					// Because Java generics are erased, we could get a wrong
					// key type
					Map.Entry<?, ?> oo1 = o1;
					Map.Entry<?, ?> oo2 = o2;
					return ( oo1.getKey().toString() ).compareTo( oo2.getKey().toString() );
				}
			}
		} );

		LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
		for( Map.Entry<K, V> entry : list )
			result.put( entry.getKey(), entry.getValue() );
		return result;
	}
}

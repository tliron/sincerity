/**
 * Copyright 2011-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity;

import java.io.File;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.threecrickets.scripturian.internal.ServiceLoader;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * Can manage the collection of Sincerity plugins for a {@link Container}, and
 * also the "system" plugins that are part of the Sincerity base installation.
 * <p>
 * Because the set of plugins in a container depends on its classpath, instance
 * of this class are associated with the {@link Dependencies} class.
 * <p>
 * Note that this class implements an <i>unmodifiable</i> map.
 * 
 * @author Tal Liron
 * @see Container#getPlugins()
 * @see Sincerity#getPlugins()
 */
public class Plugins extends AbstractMap<String, Plugin1>
{
	//
	// Construction
	//

	/**
	 * @param container
	 *        The container
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Plugins( Container container ) throws SincerityException
	{
		this( container.getSincerity(), container );
	}

	/**
	 * @param sincerity
	 *        The Sincerity instance
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Plugins( Sincerity sincerity ) throws SincerityException
	{
		this( sincerity, null );
	}

	/**
	 * Finds all delegated and JVM plugins in the container's classpath.
	 * 
	 * @param sincerity
	 *        The Sincerity instance
	 * @param container
	 *        The container or null
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Plugins( Sincerity sincerity, Container container ) throws SincerityException
	{
		super();

		ClassLoader classLoader = container != null ? container.getBootstrap() : getClass().getClassLoader();

		// Delegated plugins

		ScripturianShell shell = null;

		if( container != null )
		{
			File pluginsDir = container.getLibrariesFile( "scripturian", "plugins" );
			if( pluginsDir.isDirectory() )
			{
				shell = new ScripturianShell( container, true );
				for( File pluginFile : pluginsDir.listFiles() )
				{
					if( pluginFile.isHidden() )
						continue;

					try
					{
						Plugin1 plugin = new DelegatedPlugin( pluginFile, container, shell );
						plugins.put( plugin.getName(), plugin );
					}
					catch( Exception x )
					{
						container.getSincerity().dumpStackTrace( x );
					}
				}
			}
		}

		File pluginsDir = sincerity.getHomeFile( "libraries", "scripturian", "plugins" );
		if( pluginsDir.isDirectory() )
		{
			if( shell == null )
				shell = new ScripturianShell( sincerity, true );
			for( File pluginFile : pluginsDir.listFiles() )
			{
				if( pluginFile.isHidden() )
					continue;

				try
				{
					Plugin1 plugin = new DelegatedPlugin( pluginFile, sincerity, shell );
					plugins.put( plugin.getName(), plugin );
				}
				catch( Exception x )
				{
					sincerity.dumpStackTrace( x );
				}
			}
		}

		// JVM plugins
		for( Plugin1 plugin : ServiceLoader.load( Plugin1.class, classLoader ) )
			plugins.put( plugin.getName(), plugin );
	}

	//
	// AbstractMap
	//

	@Override
	public Set<Map.Entry<String, Plugin1>> entrySet()
	{
		return Collections.unmodifiableSet( plugins.entrySet() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private HashMap<String, Plugin1> plugins = new HashMap<String, Plugin1>();
}

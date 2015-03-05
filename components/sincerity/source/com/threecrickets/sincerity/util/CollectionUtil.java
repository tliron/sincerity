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

package com.threecrickets.sincerity.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility methods for collections.
 * 
 * @author Tal Liron
 */
public abstract class CollectionUtil
{
	//
	// Static operations
	//

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
	public static <K extends Comparable<? super K>, V> LinkedHashMap<K, V> sortedMap( Map<K, V> map )
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
					Logger.getLogger( CollectionUtil.class.getCanonicalName() ).warning( "Can't compare " + oo1.getKey().getClass() + "(" + oo1.getKey() + ") and " + oo2.getKey().getClass() + "(" + oo2.getKey() + ")" );
					return ( oo1.getKey().toString() ).compareTo( oo2.getKey().toString() );
				}
			}
		} );

		LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
		for( Map.Entry<K, V> entry : list )
			result.put( entry.getKey(), entry.getValue() );
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private CollectionUtil()
	{
	}
}

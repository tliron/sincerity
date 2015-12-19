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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author Tal Liron
 */
public abstract class ClassUtil
{
	//
	// Static operations
	//

	@SuppressWarnings("unchecked")
	public static <T> T newInstance( String className, Map<String, ?> config )
	{
		try
		{
			Class<T> theClass = (Class<T>) Class.forName( className );
			Constructor<T> constructor = theClass.getConstructor( Map.class );
			return constructor.newInstance( config );
		}
		catch( ClassNotFoundException x )
		{
			throw new RuntimeException( x );
		}
		catch( NoSuchMethodException x )
		{
			throw new RuntimeException( x );
		}
		catch( SecurityException x )
		{
			throw new RuntimeException( x );
		}
		catch( InstantiationException x )
		{
			throw new RuntimeException( x );
		}
		catch( IllegalAccessException x )
		{
			throw new RuntimeException( x );
		}
		catch( IllegalArgumentException x )
		{
			throw new RuntimeException( x );
		}
		catch( InvocationTargetException x )
		{
			throw new RuntimeException( x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private ClassUtil()
	{
	}
}

/**
 * Copyright 2011-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * Class utilities.
 * 
 * @author Tal Liron
 */
public abstract class ClassUtil
{
	//
	// Static operations
	//

	/**
	 * Executes the main() method of the class named by the first argument using
	 * the current bootstrap.
	 * 
	 * @param sincerity
	 *        The Sincerity instance
	 * @param arguments
	 *        The class name followed by the arguments for main()
	 * @throws SincerityException
	 *         In case of an error
	 */
	public static void main( Sincerity sincerity, String[] arguments ) throws SincerityException
	{
		String mainClassName = arguments[0];
		String[] mainArguments = new String[arguments.length - 1];
		System.arraycopy( arguments, 1, mainArguments, 0, mainArguments.length );
		main( sincerity, mainClassName, mainArguments );
	}

	/**
	 * Executes the main() method of a class using the current bootstrap.
	 * 
	 * @param sincerity
	 *        The Sincerity instance
	 * @param className
	 *        The class name
	 * @param arguments
	 *        The arguments for main()
	 * @throws SincerityException
	 *         In case of an error
	 */
	public static void main( Sincerity sincerity, String className, String[] arguments ) throws SincerityException
	{
		if( sincerity == null )
		{
			sincerity = Sincerity.getCurrent();
			if( sincerity == null )
				throw new SincerityException( "You can only call this method from within Sincerity" );
		}

		try
		{
			ClassLoader classLoader;
			try
			{
				classLoader = sincerity.getContainer().getBootstrap();
			}
			catch( NoContainerException x )
			{
				classLoader = ClassUtil.class.getClassLoader();
			}

			Class<?> theClass = Class.forName( className, true, classLoader );
			Method mainMethod = theClass.getMethod( "main", String[].class );
			mainMethod.invoke( null, (Object) arguments );
		}
		catch( ClassNotFoundException x )
		{
			throw new SincerityException( "Could not find class: " + className, x );
		}
		catch( SecurityException x )
		{
			throw new SincerityException( "Could not access class: " + className, x );
		}
		catch( NoSuchMethodException x )
		{
			throw new SincerityException( "Class does not have a main method: " + className, x );
		}
		catch( IllegalArgumentException x )
		{
			throw new SincerityException( "Class error: " + className, x );
		}
		catch( IllegalAccessException x )
		{
			throw new SincerityException( "Could not access class: " + className, x );
		}
		catch( InvocationTargetException x )
		{
			throw new SincerityException( x.getCause().getMessage(), x.getCause() );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private ClassUtil()
	{
	}
}

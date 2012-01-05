package com.threecrickets.sincerity.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;

public class ClassUtil
{
	//
	// Static operations
	//

	public static void main( Sincerity sincerity, String[] arguments ) throws SincerityException
	{
		String mainClassName = arguments[0];
		String[] mainArguments = new String[arguments.length - 1];
		System.arraycopy( arguments, 1, mainArguments, 0, mainArguments.length );
		main( sincerity, mainClassName, mainArguments );
	}

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
			Class<?> theClass = Class.forName( className, true, sincerity.getContainer().getBoostrap() );
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

}

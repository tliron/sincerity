package com.threecrickets.sincerity.plugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class MainPlugin implements Plugin
{
	//
	// Static operations
	//

	public static void main( Sincerity sincerity, String className, String[] arguments ) throws SincerityException
	{
		try
		{
			Class<?> theClass = sincerity.getContainer().getDependencies().getClassLoader().loadClass( className );
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

	//
	// Plugin
	//

	public String getName()
	{
		return "main";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"main"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "main".equals( commandName ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "main class name" );

			String mainClassName = arguments[0];
			String[] mainArguments = new String[arguments.length - 1];
			System.arraycopy( arguments, 1, mainArguments, 0, mainArguments.length );

			main( command.getSincerity(), mainClassName, mainArguments );
		}
		else
			throw new UnknownCommandException( command );
	}
}

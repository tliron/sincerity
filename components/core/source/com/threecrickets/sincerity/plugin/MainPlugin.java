package com.threecrickets.sincerity.plugin;

import java.lang.reflect.Method;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class MainPlugin implements Plugin
{
	//
	// Static operations
	//

	public static void main( Sincerity sincerity, String className, String[] arguments ) throws Exception
	{
		Class<?> theClass = sincerity.getContainer().getDependencies().getClassLoader().loadClass( className );
		Method mainMethod = theClass.getMethod( "main", String[].class );
		mainMethod.invoke( null, (Object) arguments );
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

	public void run( Command command, Sincerity sincerity ) throws Exception
	{
		if( "main".equals( command.name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "main class name" );

			String mainClassName = arguments[0];
			String[] mainArguments = new String[arguments.length - 1];
			System.arraycopy( arguments, 1, mainArguments, 0, mainArguments.length );

			main( sincerity, mainClassName, mainArguments );
		}
		else
			throw new UnknownCommandException( command );
	}
}

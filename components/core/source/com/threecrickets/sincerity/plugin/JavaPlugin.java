package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

public class JavaPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "java";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"compile"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String commandName = command.getName();
		if( "compile".equals( commandName ) )
		{
			Container container = command.getSincerity().getContainer();
			Dependencies dependencies = container.getDependencies();

			File javaDir = container.getFile( "libraries", "java" );
			File classesDir = container.getFile( "libraries", "classes" );

			if( !javaDir.isDirectory() )
				return;

			try
			{
				Class<?> javac = dependencies.getClassLoader().loadClass( "com.sun.tools.javac.Main" );
				Method compileMethod = javac.getMethod( "compile", String[].class );

				ArrayList<String> compileArguments = new ArrayList<String>();
				compileArguments.add( "-d" );
				compileArguments.add( classesDir.getAbsolutePath() );
				compileArguments.add( "-classpath" );
				compileArguments.add( dependencies.getClasspath() );
				addSources( javaDir, compileArguments );

				classesDir.mkdirs();

				compileMethod.invoke( null, (Object) compileArguments.toArray( new String[compileArguments.size()] ) );

				dependencies.reload();
			}
			catch( ClassNotFoundException x )
			{
				throw new SincerityException( "Could not find Java compiler in classpath. Perhaps JAVA_HOME is not set?", x );
			}
			catch( SecurityException x )
			{
				throw new SincerityException( "Could not access to Java compiler", x );
			}
			catch( NoSuchMethodException x )
			{
				throw new SincerityException( "Java compiler is not executable", x );
			}
			catch( IllegalArgumentException x )
			{
				throw new SincerityException( "Java compiler error", x );
			}
			catch( IllegalAccessException x )
			{
				throw new SincerityException( "Could not access to Java compiler", x );
			}
			catch( InvocationTargetException x )
			{
				throw new SincerityException( "Java compilation error: " + x.getCause().getMessage(), x.getCause() );
			}
		}
		else
			throw new UnknownCommandException( command );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static void addSources( File dir, Collection<String> list )
	{
		for( File file : dir.listFiles() )
		{
			if( file.isDirectory() )
				addSources( file, list );
			else if( file.getName().endsWith( ".java" ) )
				list.add( file.getAbsolutePath() );
		}
	}
}

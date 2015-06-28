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

package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

/**
 * The Java plugin supports the following commands:
 * <ul>
 * <li><b>compile</b>: compiles .java files using the container's classpath. The
 * optional argument is the root directory of the .java files, otherwise the
 * default would be "/libraries/java/". The second argument, also optional, is
 * the output directory, which defaults to "/libraries/classes/". Note that this
 * command require you to be running in a JVM environment that supports
 * compilation (a JDK).</li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class JavaPlugin implements Plugin1
{
	//
	// Plugin
	//

	public int getInterfaceVersion()
	{
		return 1;
	}

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
		Sincerity sincerity = command.getSincerity();

		if( "compile".equals( commandName ) )
		{
			Container container = sincerity.getContainer();

			String[] arguments = command.getArguments();
			File javaDir;
			if( arguments.length > 0 )
				javaDir = new File( arguments[0] );
			else
				javaDir = container.getFile( "libraries", "java" );

			if( !javaDir.isDirectory() )
				return;

			File classesDir;
			if( arguments.length > 1 )
				classesDir = new File( arguments[1] );
			else
				classesDir = container.getFile( "libraries", "classes" );

			try
			{
				Class<?> javac = container.getBootstrap().loadClass( "com.sun.tools.javac.Main" );
				Method compileMethod = javac.getMethod( "compile", String[].class );

				ArrayList<String> compileArguments = new ArrayList<String>();
				compileArguments.add( "-d" );
				compileArguments.add( classesDir.getAbsolutePath() );
				compileArguments.add( "-classpath" );
				compileArguments.add( container.getDependencies().getClasspath( true ) );
				addSources( javaDir, compileArguments );

				classesDir.mkdirs();

				Object r = compileMethod.invoke( null, (Object) compileArguments.toArray( new String[compileArguments.size()] ) );
				if( (Integer) r != 0 )
					throw new SincerityException( "Java compilation error" );
			}
			catch( ClassNotFoundException x )
			{
				throw new SincerityException( "Could not find Java compiler in classpath. Perhaps JAVA_HOME is not pointing to a full JDK?", x );
			}
			catch( SecurityException x )
			{
				throw new SincerityException( "Could not access Java compiler", x );
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
				throw new SincerityException( "Could not access Java compiler", x );
			}
			catch( InvocationTargetException x )
			{
				throw new SincerityException( "Java compilation error: " + x.getCause().getMessage(), x.getCause() );
			}
		}
		else
			throw new UnknownCommandException( command );
	}

	public void gui( Command command ) throws SincerityException
	{
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

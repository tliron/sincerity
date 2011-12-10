package com.threecrickets.sincerity.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
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

	public void run( Command command ) throws Exception
	{
		String name = command.getName();
		if( "compile".equals( name ) )
		{
			File root = command.getSincerity().getContainer().getRoot();
			File javaPath = new File( root, "libraries/java" );
			File classesPath = new File( root, "libraries/classes" );

			if( !javaPath.isDirectory() )
				return;

			classesPath.mkdirs();

			try
			{
				Class<?> javac = command.getSincerity().getContainer().getDependencies().getClassLoader().loadClass( "com.sun.tools.javac.Main" );
				Method compileMethod = javac.getMethod( "compile", String[].class );

				ArrayList<String> compileArguments = new ArrayList<String>();
				compileArguments.add( "-d" );
				compileArguments.add( classesPath.getAbsolutePath() );
				addSources( javaPath, compileArguments );

				compileMethod.invoke( null, (Object) compileArguments.toArray( new String[compileArguments.size()] ) );
			}
			catch( ClassNotFoundException x )
			{
				throw new Exception( "Could not find Java compiler in classpath. Perhaps JAVA_HOME is not set?" );
			}
		}
		else
			throw new UnknownCommandException( command );
	}

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

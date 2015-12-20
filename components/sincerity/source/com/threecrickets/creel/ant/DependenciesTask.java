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

package com.threecrickets.creel.ant;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.resources.FileResource;

import com.threecrickets.creel.Artifact;
import com.threecrickets.creel.Manager;
import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;

/**
 * build.xml:
 * 
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;project name="Sincerity" default="compile" xmlns:creel="antlib:com.threecrickets.creel.ant"&gt;
 * 	&lt;taskdef uri="antlib:com.threecrickets.creel.ant" resource="com/threecrickets/creel/ant/antlib.xml" classpath="creel.jar"/&gt;
 *  &lt;target name="dependencies&gt;
 * 	  &lt;creel:dependencies conflictPolicy="newest" destDir="lib" pathid="my.dependencies.classpath"&gt;
 * 	    &lt;module group="com.github.sommeri" name="less4j" version="(,1.15.2)"/&gt;
 * 	    &lt;module group="org.jsoup" name="jsoup" version="1.8.1"/&gt;
 *      &lt;repository id="restlet" url="http://maven.restlet.com" all="false"/&gt;
 *      &lt;repository id="central" url="https://repo1.maven.org/maven2/"/&gt;
 *    &lt;/creel:dependencies&gt;
 *    &lt;echo&gt;${toString:my.dependencies.classpath}&lt;/echo&gt;
 *  &lt;/target&gt;
 *  &lt;target name="compile" depends="dependencies"&gt;
 *    &lt;javac srcdir="." classpathref="my.dependencies.classpath"&gt;
 *      &lt;include name="Test.java"/&gt;
 * 	  &lt;/javac&gt;
 *   &lt;/target&gt;
 * &lt;/project&gt;
 * </pre>
 * 
 * Test.java:
 * 
 * <pre>
 * {@code
 * import org.jsoup.Jsoup;
 * public class Test {}
 * }
 * </pre>
 * 
 * @author Tal Liron
 */
public class DependenciesTask extends Task
{
	//
	// Attributes
	//

	public void setPathId( String pathId )
	{
		this.pathId = pathId;
	}

	public void setDestDir( FileResource destDir )
	{
		this.destDir = destDir;
	}

	public void setOverwrite( boolean overwrite )
	{
		this.overwrite = overwrite;
	}

	public void setDefaultPlatform( String defaultPlatform )
	{
		this.defaultPlatform = defaultPlatform;
	}

	public void setConflictPolicy( String conflictPolicy )
	{
		this.conflictPolicy = conflictPolicy;
	}

	public void setQuiet( boolean quiet )
	{
		this.quiet = quiet;
	}

	//
	// Operations
	//

	public Config createModule()
	{
		Config config = new Config();
		modules.add( config );
		return config;
	}

	public Config createRepository()
	{
		Config config = new Config();
		repositories.add( config );
		return config;
	}

	//
	// Task
	//

	@Override
	public void execute()
	{
		Manager manager = new Manager();
		if( conflictPolicy != null )
		{
			if( "newest".equalsIgnoreCase( conflictPolicy ) )
				manager.setConflictPolicy( Manager.ConflictPolicy.NEWEST );
			else if( "oldest".equalsIgnoreCase( conflictPolicy ) )
				manager.setConflictPolicy( Manager.ConflictPolicy.OLDEST );
			else
				throw new BuildException( "Unsupported conflict policy: " + conflictPolicy );
		}

		if( !quiet )
			( (EventHandlers) manager.getEventHandler() ).add( new ConsoleEventHandler( new PrintWriter( System.out, true ), false ) );

		if( defaultPlatform != null )
			manager.setDefaultPlatform( defaultPlatform );

		manager.setExplicitModules( modules );
		manager.setRepositories( repositories );
		manager.identify();
		Iterable<Artifact> artifacts = manager.install( destDir.getFile(), overwrite, true );

		if( pathId != null )
		{
			Path path = new Path( getProject() );
			for( Artifact artifact : artifacts )
				path.createPathElement().setLocation( artifact.getFile() );
			getProject().getReferences().put( pathId, path );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Collection<Map<String, ?>> modules = new ArrayList<Map<String, ?>>();

	private final Collection<Map<String, ?>> repositories = new ArrayList<Map<String, ?>>();

	private String pathId;

	private FileResource destDir = new FileResource( new File( "." ) );

	private boolean overwrite;

	private String defaultPlatform;

	private String conflictPolicy;

	private boolean quiet;
}

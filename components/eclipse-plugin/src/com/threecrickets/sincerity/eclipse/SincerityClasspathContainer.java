package com.threecrickets.sincerity.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

public class SincerityClasspathContainer implements IClasspathContainer
{
	//
	// Constants
	//

	public final static Path ID = new Path( "com.threecrickets.sincerity.classpathContainer" );

	//
	// Construction
	//

	public SincerityClasspathContainer( IProject project )
	{
		this.project = project;
	}

	//
	// IClasspathContainer
	//

	public String getDescription()
	{
		return DESCRIPTION;
	}

	public int getKind()
	{
		return KIND;
	}

	public IPath getPath()
	{
		return ID;
	}

	public IClasspathEntry[] getClasspathEntries()
	{
		ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();

		ArrayList<File> jars = new ArrayList<File>();
		File libraries = new File( project.getLocation().toFile(), "libraries" );
		addJars( libraries, jars );
		for( File jar : jars )
		{
			IClasspathEntry entry = JavaCore.newLibraryEntry( new Path( jar.getAbsolutePath() ), null, new Path( "/" ), new IAccessRule[0], null, false );
			entries.add( entry );
		}

		return entries.toArray( new IClasspathEntry[entries.size()] );
	}

	private final static int KIND = IClasspathContainer.K_APPLICATION;

	private final static String DESCRIPTION = "Sincerity Libraries";

	private final IProject project;

	private static void addJars( File file, Collection<File> jars )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				addJars( child, jars );
		else if( file.getName().endsWith( ".jar" ) )
			jars.add( file );
	}
}

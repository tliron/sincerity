package com.threecrickets.sincerity.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;

import com.threecrickets.sincerity.eclipse.internal.AbstractSincerityClasspathContainer;

public class SincerityClasspathContainer extends AbstractSincerityClasspathContainer
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
		jarsDir = new File( new File( project.getLocation().toFile(), "libraries" ), "jars" );
	}

	//
	// Attributes
	//

	public boolean has( File file )
	{
		String path = file.getPath();
		String jarsPath = jarsDir.getPath() + File.separatorChar;
		return path.startsWith( jarsPath );
	}

	//
	// IClasspathContainer
	//

	public String getDescription()
	{
		return DESCRIPTION;
	}

	public IPath getPath()
	{
		return ID;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected Collection<IClasspathEntry> getClasspathEntriesCollection()
	{
		ArrayList<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		addJars( jarsDir, entries );
		return entries;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final static String DESCRIPTION = "Sincerity Container Libraries";

	private final File jarsDir;
}

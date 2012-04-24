package com.threecrickets.sincerity.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;

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
		File root = project.getLocation().toFile();
		jarsDir = new File( new File( root, "libraries" ), "jars" );
		apiDir = new File( new File( root, "reference" ), "api" );
		sourceDir = new File( new File( root, "reference" ), "source" );
		nativeDir = new File( new File( root, "libraries" ), "native" );
	}

	//
	// Attributes
	//

	public boolean has( File file )
	{
		if( file.equals( jarsDir ) )
			return true;
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
		addJars( jarsDir, apiDir, sourceDir, entries );

		if( nativeDir.isDirectory() )
		{
			entries.add( JavaCore.newLibraryEntry( new Path( nativeDir.getAbsolutePath() ), null, null, null, new IClasspathAttribute[]
			{
				JavaRuntime.newLibraryPathsAttribute( new String[]
				{
					nativeDir.getAbsolutePath()
				} ), JavaCore.newClasspathAttribute( IClasspathAttribute.OPTIONAL, "true" )
			}, false ) );
		}

		return entries;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final static String DESCRIPTION = "Sincerity Container Dependencies";

	private final File jarsDir;

	private final File apiDir;

	private final File sourceDir;

	private final File nativeDir;
}

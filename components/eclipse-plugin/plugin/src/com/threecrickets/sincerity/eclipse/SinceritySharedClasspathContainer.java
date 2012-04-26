package com.threecrickets.sincerity.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;

import com.threecrickets.sincerity.eclipse.internal.AbstractSincerityClasspathContainer;
import com.threecrickets.sincerity.eclipse.internal.Text;

public class SinceritySharedClasspathContainer extends AbstractSincerityClasspathContainer
{
	//
	// Constants
	//

	public final static Path ID = new Path( SincerityPlugin.ID + ".sharedClasspathContainer" );

	//
	// IClasspathContainer
	//

	public String getDescription()
	{
		return Text.SharedClasspathName;
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

		File root = SincerityPlugin.getDefault().getSincerityRoot();
		if( root != null )
		{
			File jarsDir = new File( new File( root, "libraries" ), "jars" );
			File apiDir = new File( new File( root, "reference" ), "api" );
			File sourceDir = new File( new File( root, "reference" ), "source" );

			addJars( new File( root, "bootstrap.jar" ), entries );
			addJars( jarsDir, apiDir, sourceDir, entries );
		}

		return entries;
	}
}

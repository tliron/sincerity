package com.threecrickets.sincerity.eclipse.internal;

import java.io.File;
import java.util.Collection;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;

public abstract class AbstractSincerityClasspathContainer implements IClasspathContainer
{
	//
	// IClasspathContainer
	//

	public int getKind()
	{
		return KIND;
	}

	public IClasspathEntry[] getClasspathEntries()
	{
		Collection<IClasspathEntry> entries = getClasspathEntriesCollection();
		return entries.toArray( new IClasspathEntry[entries.size()] );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected abstract Collection<IClasspathEntry> getClasspathEntriesCollection();

	protected static void addJars( File file, Collection<IClasspathEntry> entries )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				addJars( child, entries );
		else if( file.getName().endsWith( ".jar" ) )
		{
			// TODO: sources and javadocs?
			IClasspathEntry entry = JavaCore.newLibraryEntry( new Path( file.getAbsolutePath() ), null, ROOT_PATH, new IAccessRule[0], null, false );
			entries.add( entry );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final static int KIND = IClasspathContainer.K_APPLICATION;

	private final static Path ROOT_PATH = new Path( "/" );
}

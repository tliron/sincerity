package com.threecrickets.sincerity.eclipse.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Collection;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
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

	protected static void addJars( File jarsDir, Collection<IClasspathEntry> entries )
	{
		addJars( jarsDir, null, null, null, entries );
	}

	protected static void addJars( File jarsDir, File apiDir, File sourceDir, Collection<IClasspathEntry> entries )
	{
		addJars( jarsDir, jarsDir, apiDir, sourceDir, entries );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final static int KIND = IClasspathContainer.K_APPLICATION;

	private static void addJars( File file, File jarsDir, File apiDir, File sourceDir, Collection<IClasspathEntry> entries )
	{
		if( file.isDirectory() )
			for( File child : file.listFiles() )
				addJars( child, jarsDir, apiDir, sourceDir, entries );
		else if( file.getName().endsWith( ".jar" ) )
		{
			IPath sourcePath = null;
			IClasspathAttribute[] attributes = null;

			if( jarsDir != null )
			{
				File version = file.getParentFile();
				File name = version.getParentFile();
				File group = name.getParentFile();
				if( group.getParentFile().equals( jarsDir ) )
				{
					File api = new File( new File( new File( apiDir, group.getName() ), name.getName() ), version.getName() );
					if( api.isDirectory() )
					{
						File apiFile = new File( api, name.getName() + ".jar" );
						if( apiFile.exists() )
							api = apiFile;

						try
						{
							IClasspathAttribute attribute = JavaCore.newClasspathAttribute( IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, api.getAbsoluteFile().toURI().toURL().toString() );
							attributes = new IClasspathAttribute[]
							{
								attribute
							};
						}
						catch( MalformedURLException x )
						{
						}
					}

					File source = new File( new File( new File( sourceDir, group.getName() ), name.getName() ), version.getName() );
					if( source.isDirectory() )
					{
						File sourceFile = new File( source, name.getName() + ".jar" );
						if( sourceFile.exists() )
							source = sourceFile;

						sourcePath = new Path( source.getAbsolutePath() );
					}
				}
			}

			IClasspathEntry entry = JavaCore.newLibraryEntry( new Path( file.getAbsolutePath() ), sourcePath, null, null, attributes, false );
			entries.add( entry );
		}
	}
}

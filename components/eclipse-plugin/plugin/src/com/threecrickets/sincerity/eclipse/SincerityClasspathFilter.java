package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class SincerityClasspathFilter extends ViewerFilter
{
	//
	// ViewerFilter
	//

	@Override
	public boolean select( Viewer viewer, Object parentElement, Object element )
	{
		if( element instanceof IFolder )
		{
			IFolder file = (IFolder) element;
			IJavaProject javaProject = JavaCore.create( file.getProject() );
			try
			{
				IClasspathEntry[] entries = javaProject.getRawClasspath();
				for( IClasspathEntry entry : entries )
				{
					if( ( entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER ) && ( SincerityClasspathContainer.ID.isPrefixOf( entry.getPath() ) ) )
					{
						SincerityClasspathContainer container = (SincerityClasspathContainer) JavaCore.getClasspathContainer( entry.getPath(), javaProject );
						if( container.has( file.getLocation().toFile() ) )
						{
							// SincerityPlugin.getSimpleLog().log( IStatus.INFO,
							// "Managed by Sincerity: " + element );
							return false;
						}
					}
				}
			}
			catch( Exception x )
			{
				SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
			}
			// SincerityPlugin.getSimpleLog().log( IStatus.INFO,
			// "Not managed by Sincerity: " + element );
		}

		return true;
	}
}

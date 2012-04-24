package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class SinceritySharedClasspathContainerInitializer extends ClasspathContainerInitializer
{
	//
	// ClasspathContainerInitializer
	//

	@Override
	public void initialize( IPath path, IJavaProject project ) throws CoreException
	{
		SinceritySharedClasspathContainer container = new SinceritySharedClasspathContainer();
		JavaCore.setClasspathContainer( path, new IJavaProject[]
		{
			project
		}, new IClasspathContainer[]
		{
			container
		}, null );
	}

	@Override
	public void requestClasspathContainerUpdate( IPath path, IJavaProject project, IClasspathContainer containerSuggestion ) throws CoreException
	{
	}

	@Override
	public boolean canUpdateClasspathContainer( IPath path, IJavaProject project )
	{
		return false;
	}
}

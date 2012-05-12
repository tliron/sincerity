package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

import com.threecrickets.sincerity.eclipse.internal.EclipseUtil;

public class SincerityNature implements IProjectNature
{
	//
	// Constants
	//

	public static final String ID = SincerityPlugin.ID + ".nature";

	//
	// IProjectNature
	//

	public void configure() throws CoreException
	{
		if( project.hasNature( JavaCore.NATURE_ID ) )
		{
			IJavaProject javaProject = JavaCore.create( project );
			EclipseUtil.addClasspathContainer( javaProject, new SinceritySharedClasspathContainer() );
			EclipseUtil.addClasspathContainer( javaProject, new SincerityClasspathContainer( project ) );
		}

		EclipseUtil.addBuilder( project, SincerityBuilder.ID );
	}

	public void deconfigure() throws CoreException
	{
		EclipseUtil.removeBuilder( project, SincerityBuilder.ID );
	}

	public IProject getProject()
	{
		return project;
	}

	public void setProject( IProject project )
	{
		this.project = project;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private IProject project;
}

package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class SincerityNature implements IProjectNature
{
	//
	// Constants
	//

	public static final String ID = "com.threecrickets.sincerity.container";

	//
	// IProjectNature
	//

	@Override
	public void configure() throws CoreException
	{
		EclipseUtil.addBuilder( project, SincerityBuilder.ID );
	}

	@Override
	public void deconfigure() throws CoreException
	{
		EclipseUtil.removeBuilder( project, SincerityBuilder.ID );
	}

	@Override
	public IProject getProject()
	{
		return project;
	}

	@Override
	public void setProject( IProject project )
	{
		this.project = project;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private IProject project;
}

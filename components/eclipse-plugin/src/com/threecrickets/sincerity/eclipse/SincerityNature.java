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

	public void configure() throws CoreException
	{
		try
		{
			SincerityBootstrap sincerityBoostrap = new SincerityBootstrap( SincerityNature.class.getClassLoader() );
			sincerityBoostrap.main( "container:create", "--force", project.getLocation().toOSString() );
		}
		catch( Exception x )
		{
			x.printStackTrace();
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

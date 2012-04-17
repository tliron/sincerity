package com.threecrickets.sincerity.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class SincerityConvertCommand implements IObjectActionDelegate
{
	//
	// IObjectActionDelegate
	//

	public void run( IAction action )
	{
		System.out.println( "Converting..." );
		for( IProject project : projects )
		{
			try
			{
				EclipseUtil.addNature( project, SincerityNature.ID );
			}
			catch( CoreException x )
			{
				x.printStackTrace();
			}
			System.out.println( "Convert to Sincerity container: " + project );
		}
		// MessageDialog.openInformation( null, "Sincerity",
		// "Hello, Eclipse world" );
	}

	public void selectionChanged( IAction action, ISelection selection )
	{
		projects.clear();
		for( IProject project : EclipseUtil.getSelectedProjects( selection ) )
		{
			try
			{
				if( project.getNature( SincerityNature.ID ) == null )
				{
					projects.add( project );
					System.out.println( "Added project: " + project );
				}
			}
			catch( CoreException x )
			{
				x.printStackTrace();
			}
		}
	}

	public void setActivePart( IAction action, IWorkbenchPart workbenchPart )
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<IProject> projects = new ArrayList<IProject>();
}

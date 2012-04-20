package com.threecrickets.sincerity.eclipse;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Unused.
 * 
 * @author Tal Liron
 */
public class SincerityConvertCommandHandler implements IObjectActionDelegate
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
				SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
			}
		}
	}

	public void selectionChanged( IAction action, ISelection selection )
	{
		projects.clear();
		try
		{
			for( IProject project : EclipseUtil.getSelectedProjects( selection, false, SincerityNature.ID ) )
			{
				projects.add( project );
				System.out.println( "Added project: " + project );
			}
		}
		catch( CoreException x )
		{
			SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
		}
	}

	public void setActivePart( IAction action, IWorkbenchPart workbenchPart )
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<IProject> projects = new ArrayList<IProject>();
}

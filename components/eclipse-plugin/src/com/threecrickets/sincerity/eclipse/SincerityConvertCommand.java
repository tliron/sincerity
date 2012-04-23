package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

import com.threecrickets.sincerity.eclipse.internal.EclipseUtil;

public class SincerityConvertCommand extends AbstractHandler
{
	//
	// AbstractHandler
	//

	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		ISelection selection = HandlerUtil.getCurrentSelection( event );

		try
		{
			for( IProject project : EclipseUtil.getSelectedProjects( selection, false, SincerityNature.ID ) )
			{
				EclipseUtil.addNature( project, SincerityNature.ID );
				SincerityPlugin.getSimpleLog().log( IStatus.INFO, "Convert to Sincerity container: " + project );
			}
		}
		catch( CoreException x )
		{
			SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
		}

		return null;
	}
}

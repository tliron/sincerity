package com.threecrickets.sincerity.eclipse;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class SincerityGuiCommand extends AbstractHandler
{
	//
	// AbstractHandler
	//

	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		ISelection selection = HandlerUtil.getCurrentSelection( event );
		try
		{
			List<IProject> projects = EclipseUtil.getSelectedProjects( selection, true, null );
			if( projects.size() == 1 )
			{
				IProject project = projects.get( 0 );
				SincerityBootstrap sincerityBoostrap = new SincerityBootstrap( getClass().getClassLoader() );
				sincerityBoostrap.main( "container:use", project.getLocation().toOSString(), ":", "gui:gui" );
			}
		}
		catch( Exception x )
		{
			SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
		}

		// IWorkbenchWindow window =
		// HandlerUtil.getActiveWorkbenchWindowChecked( event );
		// MessageDialog.openInformation( window.getShell(), "Sincerity",
		// "Hello, Eclipse world" );
		return null;
	}
}

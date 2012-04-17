package com.threecrickets.sincerity.eclipse;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SincerityGuiCommand extends AbstractHandler
{
	/**
	 * The constructor.
	 */
	public SincerityGuiCommand()
	{
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */
	public Object execute( ExecutionEvent event ) throws ExecutionException
	{
		ISelection selection = HandlerUtil.getCurrentSelection( event );
		List<IProject> projects = EclipseUtil.getSelectedProjects( selection );
		if( projects.size() == 1 )
		{
			IProject project = projects.get( 0 );
			try
			{
				SincerityBootstrap sincerityBoostrap = new SincerityBootstrap( SincerityNature.class.getClassLoader() );
				sincerityBoostrap.main( "container:use", project.getLocation().toOSString(), ":", "gui:gui" );
			}
			catch( Exception x )
			{
				x.printStackTrace();
			}
		}

		// IWorkbenchWindow window =
		// HandlerUtil.getActiveWorkbenchWindowChecked( event );
		// MessageDialog.openInformation( window.getShell(), "Sincerity",
		// "Hello, Eclipse world" );
		return null;
	}
}

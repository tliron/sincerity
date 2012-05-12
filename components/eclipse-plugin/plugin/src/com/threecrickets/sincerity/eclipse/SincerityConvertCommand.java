package com.threecrickets.sincerity.eclipse;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import com.threecrickets.sincerity.eclipse.internal.EclipseUtil;
import com.threecrickets.sincerity.eclipse.internal.SincerityBootstrap;
import com.threecrickets.sincerity.eclipse.internal.Text;

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
				if( convert( project ) )
				{
					EclipseUtil.addNature( project, SincerityNature.ID );
					SincerityPlugin.getSimpleLog().log( IStatus.INFO, "Convert to Sincerity container: " + project );
				}
			}
		}
		catch( CoreException x )
		{
			SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
		}

		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String SINCERITY_DIR = ".sincerity";

	private static boolean convert( IProject project )
	{
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

		File sincerityDir = new File( project.getLocation().toFile(), SINCERITY_DIR );
		boolean force = false;
		boolean isContainer = sincerityDir.isDirectory();
		final String[] template = new String[]
		{
			"default"
		};

		if( isContainer )
		{
			MessageBox messageBox = new MessageBox( shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO );
			messageBox.setText( Text.ConvertTitle );
			messageBox.setMessage( Text.ConvertAlreadyContainer );
			if( messageBox.open() == SWT.YES )
				force = true;
		}

		if( !isContainer || force )
		{
			final Shell dialog = new Shell( shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM );
			dialog.setText( Text.ConvertTitle );
			FillLayout layout = new FillLayout();
			layout.marginHeight = 10;
			layout.marginWidth = 10;
			dialog.setLayout( layout );

			Composite main = new Composite( dialog, SWT.NO_BACKGROUND );
			main.setLayout( new GridLayout( 1, false ) );

			final String[] templates = getTemplates();
			final boolean[] create = new boolean[]
			{
				false
			};

			Label label = new Label( main, SWT.NONE );
			label.setText( Text.ConvertChoose );

			final Combo templatesCombo = new Combo( main, SWT.DROP_DOWN | SWT.READ_ONLY );
			for( int i = 0, length = templates.length; i < length; i++ )
			{
				templatesCombo.add( templates[i] );
				if( "default".equals( templates[i] ) )
					templatesCombo.select( i );
			}

			new Label( main, SWT.NONE );

			Composite buttons = new Composite( main, SWT.NO_BACKGROUND );
			buttons.setLayoutData( new GridData( SWT.END, SWT.CENTER, true, false ) );
			buttons.setLayout( new FillLayout() );

			Button createButton = new Button( buttons, SWT.PUSH );
			createButton.setText( "Create" );
			createButton.addListener( SWT.Selection, new Listener()
			{
				public void handleEvent( Event event )
				{
					create[0] = true;
					template[0] = templates[templatesCombo.getSelectionIndex()];
					dialog.dispose();
				}
			} );

			Button cancelButton = new Button( buttons, SWT.PUSH );
			cancelButton.setText( "Cancel" );
			cancelButton.addListener( SWT.Selection, new Listener()
			{
				public void handleEvent( Event event )
				{
					dialog.dispose();
				}
			} );

			dialog.pack();
			dialog.open();
			EclipseUtil.waitUntilDisposed( dialog );

			if( !create[0] )
				return false;

			SincerityPlugin.getSimpleLog().log( IStatus.INFO, "Using Sincerity container template: " + template[0] );
		}

		boolean success;
		if( force )
			success = SincerityBootstrap.run( "container:create", "--force", project.getLocation().toOSString(), template[0] );
		else
			success = SincerityBootstrap.run( "container:create", project.getLocation().toOSString(), template[0] );

		if( !success )
		{
			MessageBox messageBox = new MessageBox( shell, SWT.ICON_ERROR | SWT.OK );
			messageBox.setText( Text.ConvertTitle );
			messageBox.setMessage( Text.ConvertError );
			messageBox.open();
			return false;
		}

		return true;
	}

	private static String[] getTemplates()
	{
		File homeDir = SincerityPlugin.getDefault().getSincerityRoot();

		ArrayList<String> templates = new ArrayList<String>();
		File templatesDir = new File( homeDir, "templates" );
		if( templatesDir.isDirectory() )
		{
			for( File templateDir : templatesDir.listFiles() )
			{
				if( templateDir.isDirectory() )
					templates.add( templateDir.getName() );
			}
		}

		return templates.toArray( new String[templates.size()] );
	}
}

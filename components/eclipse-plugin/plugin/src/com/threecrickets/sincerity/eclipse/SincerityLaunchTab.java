package com.threecrickets.sincerity.eclipse;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.threecrickets.sincerity.eclipse.internal.EclipseUtil;
import com.threecrickets.sincerity.eclipse.internal.SincerityBootstrap;
import com.threecrickets.sincerity.eclipse.internal.Text;

// http://www.talendforge.org/svn/tos/branches/branch-1_0/org.epic.debug/src-debug/org/epic/debug/ui/ProjectBlock.java

public class SincerityLaunchTab extends JavaMainTab
{

	//
	// AbstractLaunchConfigurationTab
	//

	@Override
	public String getName()
	{
		return "Sincerity";
	}

	@Override
	public void setDefaults( ILaunchConfigurationWorkingCopy configuration )
	{
		super.setDefaults( configuration );
		configuration.setAttribute( SincerityLaunchConfiguration.PROGRAM_OR_URI, "" );
	}

	@Override
	public void initializeFrom( ILaunchConfiguration configuration )
	{
		super.initializeFrom( configuration );
		programsCombo.setItems( getPrograms() );
		try
		{
			programsCombo.setText( configuration.getAttribute( SincerityLaunchConfiguration.PROGRAM_OR_URI, "" ) );
		}
		catch( CoreException x )
		{
			SincerityPlugin.getSimpleLog().log( IStatus.ERROR, x );
		}
	}

	@Override
	public boolean isValid( ILaunchConfiguration configuration )
	{
		if( !super.isValid( configuration ) )
			return false;

		if( programsCombo.getText().trim().length() == 0 )
		{
			setErrorMessage( Text.LaunchProgramOrUriError );
			return false;
		}

		return true;
	}

	@Override
	public void performApply( ILaunchConfigurationWorkingCopy configuration )
	{
		super.performApply( configuration );
		configuration.setAttribute( SincerityLaunchConfiguration.PROGRAM_OR_URI, programsCombo.getText().trim() );
	}

	//
	// JavaMainTab
	//

	@Override
	public void createControl( Composite parent )
	{
		super.createControl( parent );
		Composite composite = (Composite) parent.getChildren()[0];

		// Hide the "main class" control group
		// (This is awkward, but there is no other way to reuse the
		// functionality of JavaMainTab)
		Group mainGroup = (Group) composite.getChildren()[2];
		mainGroup.setVisible( false );
		mainGroup.setEnabled( false );

		Group programsGroup = EclipseUtil.createGroup( composite, Text.LaunchProgramOrUri, 1, 1, true, false );
		programsGroup.moveAbove( mainGroup );

		programsCombo = EclipseUtil.createCombo( programsGroup, SWT.DROP_DOWN, 1, GridData.FILL_HORIZONTAL, null );
		programsCombo.addModifyListener( new ModifyListener()
		{
			public void modifyText( ModifyEvent e )
			{
				updateLaunchConfigurationDialog();
			}
		} );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected void initializeMainTypeAndName( IJavaElement javaElement, ILaunchConfigurationWorkingCopy config )
	{
		config.setAttribute( IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, SincerityBootstrap.MAIN_CLASS );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Combo programsCombo;

	@SuppressWarnings("restriction")
	private String[] getPrograms()
	{
		ArrayList<String> programs = new ArrayList<String>();
		IJavaProject project = getJavaProject();
		if( project != null )
		{
			File programsDir = new File( project.getProject().getLocation().toFile(), "programs" );
			if( programsDir.isDirectory() )
			{
				for( File program : programsDir.listFiles() )
				{
					String name = program.getName();
					int lastDot = name.lastIndexOf( '.' );
					if( lastDot != -1 )
						name = name.substring( 0, lastDot );
					programs.add( name );
				}
			}
		}
		return programs.toArray( new String[programs.size()] );
	}
}

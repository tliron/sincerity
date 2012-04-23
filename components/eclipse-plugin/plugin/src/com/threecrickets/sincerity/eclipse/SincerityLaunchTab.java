package com.threecrickets.sincerity.eclipse;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaMainTab;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.threecrickets.sincerity.eclipse.internal.SincerityBootstrap;

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
	public void createControl( Composite parent )
	{
		super.createControl( parent );

		// Hide the "main class" control group
		// (This is awkward, but there is no other way to reuse the
		// functionality of JavaMainTab)
		Composite composite = (Composite) parent.getChildren()[0];
		Group mainGroup = (Group) composite.getChildren()[2];
		mainGroup.setVisible( false );
	}

	@Override
	protected void initializeMainTypeAndName( IJavaElement javaElement, ILaunchConfigurationWorkingCopy config )
	{
		config.setAttribute( IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, SincerityBootstrap.MAIN_CLASS );
	}
}

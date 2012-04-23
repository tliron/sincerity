package com.threecrickets.sincerity.eclipse;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.EnvironmentTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaClasspathTab;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaJRETab;

public class SincerityLaunchTabGroup extends AbstractLaunchConfigurationTabGroup
{
	//
	// ILaunchConfigurationTabGroup
	//

	public void createTabs( ILaunchConfigurationDialog dialog, String mode )
	{
		setTabs( new ILaunchConfigurationTab[]
		{
			new SincerityLaunchTab(), new JavaJRETab(), new JavaClasspathTab(), new SourceLookupTab(), new EnvironmentTab(), new CommonTab()
		} );
	}
}

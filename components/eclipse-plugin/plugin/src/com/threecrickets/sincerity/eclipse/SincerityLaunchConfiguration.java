package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

// See: http://www.eclipse.org/articles/Article-Launch-Framework/launch.html

public class SincerityLaunchConfiguration extends JavaLaunchDelegate
{
	//
	// JavaLaunchDelegate
	//

	@Override
	public String getProgramArguments( ILaunchConfiguration configuration ) throws CoreException
	{
		return "start component";
	}
}

package com.threecrickets.sincerity.eclipse;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

// See: http://www.eclipse.org/articles/Article-Launch-Framework/launch.html

public class SincerityLaunchConfiguration extends JavaLaunchDelegate
{
	//
	// Constants
	//

	public static final String PROGRAM_OR_URI = "com.threecrickets.sincerity.launch.PROGRAM_OR_URI";

	//
	// JavaLaunchDelegate
	//

	@Override
	public String getProgramArguments( ILaunchConfiguration configuration ) throws CoreException
	{
		String programOrUri = configuration.getAttribute( PROGRAM_OR_URI, "" );
		return "delegate:start " + programOrUri;
	}
}

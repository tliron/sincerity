package com.threecrickets.sincerity.eclipse;

import com.threecrickets.sincerity.eclipse.internal.AbstractSincerityClasspathContainerPage;

public class SincerityClasspathContainerPage extends AbstractSincerityClasspathContainerPage
{
	//
	// Construction
	//

	public SincerityClasspathContainerPage()
	{
		super( SincerityClasspathContainer.ID, "Sincerity Container Dependencies", "These are the Sincerity dependent libraries for your project." );
	}
}

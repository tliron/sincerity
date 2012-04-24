package com.threecrickets.sincerity.eclipse;

import com.threecrickets.sincerity.eclipse.internal.AbstractSincerityClasspathContainerPage;

public class SinceritySharedClasspathContainerPage extends AbstractSincerityClasspathContainerPage
{
	//
	// Construction
	//

	public SinceritySharedClasspathContainerPage()
	{
		super( SinceritySharedClasspathContainer.ID, "Sincerity Shared Libraries", "These are the libraries provided by your Sincerity installation." );
	}
}

package com.threecrickets.sincerity.eclipse;

import com.threecrickets.sincerity.eclipse.internal.AbstractSincerityClasspathContainerPage;
import com.threecrickets.sincerity.eclipse.internal.Text;

public class SinceritySharedClasspathContainerPage extends AbstractSincerityClasspathContainerPage
{
	//
	// Construction
	//

	public SinceritySharedClasspathContainerPage()
	{
		super( SinceritySharedClasspathContainer.ID, Text.SharedClasspathName, Text.SharedClasspathDescription );
	}
}

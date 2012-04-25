package com.threecrickets.sincerity.eclipse;

import com.threecrickets.sincerity.eclipse.internal.AbstractSincerityClasspathContainerPage;
import com.threecrickets.sincerity.eclipse.internal.Text;

public class SincerityClasspathContainerPage extends AbstractSincerityClasspathContainerPage
{
	//
	// Construction
	//

	public SincerityClasspathContainerPage()
	{
		super( SincerityClasspathContainer.ID, Text.ClasspathName, Text.ClasspathDescription );
	}
}

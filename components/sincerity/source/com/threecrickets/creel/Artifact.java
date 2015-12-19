package com.threecrickets.creel;

import java.io.File;
import java.net.URL;

/**
 * @author Tal Liron
 */
public class Artifact
{
	//
	// Construction
	//

	public Artifact( URL sourceUrl, File file )
	{
		super();
		this.sourceUrl = sourceUrl;
		this.file = file;
	}

	//
	// Attributes
	//

	public URL getSourceUrl()
	{
		return sourceUrl;
	}

	public File getFile()
	{
		return file;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final URL sourceUrl;

	private final File file;
}

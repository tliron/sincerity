/**
 * Copyright 2011-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.dependencies;

import java.io.File;

/**
 * @author Tal Liron
 */
public abstract class Artifact
{
	public abstract String getName();

	public abstract String getExtension();

	public abstract String getType();

	public abstract File getLocation();

	public abstract Integer getSize();
}

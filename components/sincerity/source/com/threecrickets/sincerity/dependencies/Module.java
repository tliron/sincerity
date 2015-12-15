/**
 * Copyright 2011-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.dependencies;

import java.util.Collection;

/**
 * A module actually installed by a {@link Dependencies} instance. See
 * {@link Modules}.
 * 
 * @author Tal Liron
 */
public abstract class Module
{
	//
	// Attributes
	//

	public abstract String getGroup();

	public abstract String getName();

	public abstract String getVersion();

	public abstract Collection<Module> getChildren();

	public abstract Collection<License> getLicenses();

	public abstract Collection<Artifact> getArtifacts();

	public abstract boolean isExplicitDependency();

	public abstract boolean isEvicted();
}

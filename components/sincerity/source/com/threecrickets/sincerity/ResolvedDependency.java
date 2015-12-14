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

package com.threecrickets.sincerity;

import java.util.ArrayList;

/**
 * A descriptor for a single node in the resolved dependency tree. See
 * {@link ResolvedDependencies}.
 * 
 * @author Tal Liron
 */
public class ResolvedDependency
{
	//
	// Attributes
	//

	/**
	 * Our children.
	 */
	public final ArrayList<ResolvedDependency> children = new ArrayList<ResolvedDependency>();
}

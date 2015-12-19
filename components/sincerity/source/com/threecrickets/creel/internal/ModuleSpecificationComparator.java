/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.internal;

import java.util.Comparator;

import com.threecrickets.creel.Module;

/**
 * @author Tal Liron
 */
public class ModuleSpecificationComparator implements Comparator<Module>
{
	//
	// Constants
	//

	public static final ModuleSpecificationComparator INSTANCE = new ModuleSpecificationComparator();

	//
	// Comparator
	//

	@Override
	public int compare( Module m1, Module m2 )
	{
		return m1.getSpecification().toString().compareTo( m2.getSpecification().toString() );
	}
}

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

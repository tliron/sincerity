package com.threecrickets.creel.internal;

import java.util.Comparator;

import com.threecrickets.creel.Module;

/**
 * @author Tal Liron
 */
public class ModuleIdentifierComparator implements Comparator<Module>
{
	//
	// Constants
	//

	public static final ModuleIdentifierComparator INSTANCE = new ModuleIdentifierComparator();

	//
	// Comparator
	//

	@Override
	public int compare( Module m1, Module m2 )
	{
		return m1.getIdentifier().toString().compareTo( m2.getIdentifier().toString() );
	}
}

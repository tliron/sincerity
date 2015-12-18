package com.threecrickets.creel;

/**
 * @author Tal Liron
 */
public interface Conflict
{
	//
	// Attributes
	//

	public Module getChosen();

	public Iterable<Module> getRejects();
}

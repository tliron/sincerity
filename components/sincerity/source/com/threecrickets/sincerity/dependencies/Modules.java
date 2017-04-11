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

import java.util.AbstractList;
import java.util.Collection;

import com.threecrickets.sincerity.exception.SincerityException;

/**
 * A tree of all modules actually installed by a {@link Dependencies} instance,
 * with added utility methods for querying different aspects of this data. See
 * {@link Dependencies#getModules()}.
 * <p>
 * The instance itself is a list of the root nodes of the tree (the explicit
 * dependencies). For flat access to the entire tree, use
 * {@link Modules#getAll()}.
 * 
 * @param <M>
 *        The module class
 * @author Tal Liron
 */
public abstract class Modules<M extends Module> extends AbstractList<M>
{
	//
	// Attributes
	//

	/**
	 * Cached list of all modules, whether explicit or implicit.
	 * 
	 * @return The modules
	 */
	public abstract Collection<M> getAll();

	/**
	 * Tje artifacts.
	 * 
	 * @return The artifacts
	 */
	public abstract Collection<Artifact> getArtifacts();

	/**
	 * The licenses.
	 * 
	 * @return The licenses
	 */
	public abstract Collection<License> getLicenses();

	/**
	 * All modules using a license.
	 * 
	 * @param license
	 *        The license
	 * @return The modules
	 */
	public abstract Collection<M> getByLicense( License license );

	/**
	 * The version of a module.
	 * 
	 * @param group
	 *        The module's group
	 * @param name
	 *        The module's name
	 * @return The module's version or null
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract String getVersion( String group, String name ) throws SincerityException;
}

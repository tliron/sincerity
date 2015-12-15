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

import java.util.AbstractList;
import java.util.List;

import com.threecrickets.sincerity.exception.SincerityException;

/**
 * A tree of all dependencies actually resolved by a {@link Dependencies}
 * instance, with added utility methods for querying different aspects of this
 * data. See {@link Dependencies#getResolvedDependencies()}.
 * <p>
 * The instance itself lists the root nodes of the tree. For flat access to the
 * entire tree, use {@link ResolvedDependencies#getAll()}.
 * 
 * @param <RD>
 *        The resolved dependency class
 * @author Tal Liron
 */
public abstract class ResolvedDependencies<RD extends ResolvedDependency> extends AbstractList<RD>
{
	//
	// Construction
	//

	/**
	 * Parses the latest Ivy resolution report.
	 * 
	 * @param dependencies
	 *        The dependencies instance
	 * @throws SincerityException
	 *         In case of an error
	 */
	public ResolvedDependencies( Dependencies<RD> dependencies ) throws SincerityException
	{
		super();
	}

	//
	// Attributes
	//

	/**
	 * Cached list of all resolved dependencies, whether explicit or implicit.
	 * 
	 * @return The resolved dependencies
	 */
	public abstract List<RD> getAll();

	/**
	 * Caches list of all artifacts.
	 * 
	 * @return The artifacts
	 */
	public abstract List<Artifact> getArtifacts();

	/**
	 * Cached list of all licenses.
	 * 
	 * @return The licenses
	 */
	public abstract List<License> getLicenses();

	/**
	 * List of all resolved dependencies using a license.
	 * 
	 * @param license
	 *        The license
	 * @return The resolved dependencies
	 */
	public abstract List<RD> getByLicense( License license );

	/**
	 * The resolved version of a dependency.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @return The resolved version or null
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract String getVersion( String group, String name ) throws SincerityException;
}

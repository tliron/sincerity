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

import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * Manages the source repositories for a {@link Container}.
 * <p>
 * Repositories can be organized in "sections", allowing to prioritize the list
 * according to these groups. For example, a common scheme is to have two
 * sections, "public" and "private".
 * 
 * @author Tal Liron
 * @see Container#getRepositories()
 */
public abstract class Repositories
{
	//
	// Constants
	//

	public static final String REPOSITORY_SECTION_SEPARATOR = ":";

	//
	// Attributes
	//

	/**
	 * The repositories in a section.
	 * 
	 * @param section
	 *        The section name
	 * @return The repositories or null
	 */
	public abstract Collection<Repository> get( String section );

	//
	// Operations
	//

	/**
	 * Adds a Maven resolver.
	 * 
	 * @param section
	 *        The section name
	 * @param name
	 *        The resolver name within the section
	 * @param url
	 *        The Maven root URL
	 * @return True if added
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract boolean addMaven( String section, String name, String url ) throws SincerityException;

	/**
	 * Adds a PyPI resolver.
	 * 
	 * @param section
	 *        The section name
	 * @param name
	 *        The resolver name within the section
	 * @param url
	 *        The PyPI root URL
	 * @return True if added
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract boolean addPyPi( String section, String name, String url ) throws SincerityException;

	/**
	 * Removes a resolver.
	 * 
	 * @param section
	 *        The section name
	 * @param name
	 *        The resolver name within the section
	 * @return True if removed
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract boolean remove( String section, String name ) throws SincerityException;
}

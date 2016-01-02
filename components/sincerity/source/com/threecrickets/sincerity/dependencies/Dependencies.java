/**
 * Copyright 2011-2016 Three Crickets LLC.
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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.threecrickets.sincerity.Container;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.packaging.Artifact;
import com.threecrickets.sincerity.packaging.ArtifactManager;
import com.threecrickets.sincerity.packaging.Packages;
import com.threecrickets.sincerity.util.StringUtil;

/**
 * Manages the dependencies of a {@link Container}, including its classpath of
 * Jars as well as other managed artifacts. With this class you can add or
 * remove dependencies to a Sincerity container.
 * <p>
 * Dependencies are only actually installed when
 * {@link #install(boolean, boolean)} is called. To access the installed
 * modules, see {@link #getModules()}.
 * <p>
 * To access the Jars, use {@link #getClasspaths(boolean)}. To access the
 * artifacts, use {@link #getArtifacts()} or {@link #getPackages()}. See also
 * the {@link ArtifactManager} class.
 * 
 * @param <M>
 *        The module class
 * @author Tal Liron
 */
public abstract class Dependencies<M extends Module>
{
	//
	// Construction
	//

	/**
	 * Parses the Ivy module descriptor, and loads the managed artifacts
	 * database.
	 * 
	 * @param artifactsFile
	 *        The managed artifacts database file (usually
	 *        "/configuration/sincerity/artifacts.conf")
	 * @param container
	 *        The container
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Dependencies( File artifactsFile, Container<M, ?> container ) throws SincerityException
	{
		this.artifactsFile = artifactsFile;
		this.container = container;
	}

	//
	// Attributes
	//

	/**
	 * The container.
	 * 
	 * @return The container
	 */
	public Container<M, ?> getContainer()
	{
		return container;
	}

	/**
	 * True if the module is an explicit dependency, whatever its version.
	 * 
	 * @param group
	 *        The module's group
	 * @param name
	 *        The module's name
	 * @return True if specified
	 */
	public boolean hasExplicitDependency( String group, String name )
	{
		return hasExplicitDependency( group, name, null );
	}

	/**
	 * True if the module is an explicit dependency with a particular version.
	 * 
	 * @param group
	 *        The module's group
	 * @param name
	 *        The module's name
	 * @param version
	 *        The module's version or null
	 * @return True if specified
	 */
	public abstract boolean hasExplicitDependency( String group, String name, String version );

	/**
	 * The installed modules (explicit and implicit dependencies), based on the
	 * last {@link #install(boolean, boolean)}.
	 * 
	 * @return The installed modules
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract Modules<M> getModules() throws SincerityException;

	/**
	 * Retrieves the artifacts based on currently installed packages.
	 * 
	 * @return The artifacts
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Set<Artifact> getArtifacts() throws SincerityException
	{
		return getArtifacts( false, false, false );
	}

	/**
	 * Retrieves or installs the artifacts based on currently installed
	 * packages.
	 * 
	 * @param install
	 *        True to allow installation of packages
	 * @param overwrite
	 *        True to force overwriting of existing files
	 * @param verify
	 *        Whether to verify the unpacking
	 * @return The artifacts
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract Set<Artifact> getArtifacts( boolean install, boolean overwrite, boolean verify ) throws SincerityException;

	/**
	 * The artifact manager.
	 * 
	 * @return The artifact manager
	 * @throws SincerityException
	 *         In case of an error
	 */
	public ArtifactManager getArtifactManager() throws SincerityException
	{
		return new ArtifactManager( artifactsFile, container.createPackagingContext() );
	}

	/**
	 * The packages among the currently installed artifacts.
	 * 
	 * @return The packages
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Packages getPackages() throws SincerityException
	{
		return new Packages( container.createPackagingContext() );
	}

	/**
	 * The classpath based on currently installed artifacts.
	 * 
	 * @param includeSystem
	 *        True to include the system classpath
	 * @return The classpath
	 * @throws SincerityException
	 *         In case of an error
	 * @see #getClasspaths(boolean)
	 */
	public String getClasspath( boolean includeSystem ) throws SincerityException
	{
		List<File> classpaths = getClasspaths( includeSystem );
		ArrayList<String> paths = new ArrayList<String>( classpaths.size() );
		for( File file : classpaths )
			paths.add( file.getPath() );
		return StringUtil.join( paths, File.pathSeparator );
	}

	/**
	 * The classpath based on currently installed artifacts.
	 * 
	 * @param includeSystem
	 *        True to include the system classpath
	 * @return The classpath
	 * @throws SincerityException
	 *         In case of an error
	 * @see #getClasspath(boolean)
	 */
	public abstract List<File> getClasspaths( boolean includeSystem ) throws SincerityException;

	//
	// Operations
	//

	/**
	 * Revokes all explicit and implicit dependencies.
	 * <p>
	 * Does <i>not</i> resolve; only changes the specification.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract void reset() throws SincerityException;

	/**
	 * Adds an explicit dependency.
	 * <p>
	 * Will not add the dependency if it is already specified.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @param version
	 *        The dependency's version
	 * @param force
	 *        Whether to force the dependency
	 * @param transitive
	 *        Whether to pull in dependencies of the dependency
	 * @return True if added
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract boolean addExplicitDependency( String group, String name, String version, boolean force, boolean transitive ) throws SincerityException;

	/**
	 * Changes the version for an explicit dependency that has already been
	 * specified.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @param newVersion
	 *        The dependency's new version
	 * @return True if changed
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract boolean reviseExplicitDependency( String group, String name, String newVersion ) throws SincerityException;

	/**
	 * Revokes an explicit dependency.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @return True if removed
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract boolean removeExplicitDependency( String group, String name ) throws SincerityException;

	/**
	 * Excludes an implicit dependency.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @return True if removed
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract boolean excludeDependency( String group, String name ) throws SincerityException;

	/**
	 * Overrides the version of an implicit dependency.
	 * 
	 * @param group
	 *        The dependency's group
	 * @param name
	 *        The dependency's name
	 * @param version
	 *        The dependency's version
	 * @return True if overridden
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract boolean overrideDependency( String group, String name, String version ) throws SincerityException;

	/**
	 * Sets the required versions of all explicit and implicit dependencies to
	 * the those that were last resolved.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract void freezeVersions() throws SincerityException;

	/**
	 * Deletes all artifacts which no longer have a dependency.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void prune() throws SincerityException
	{
		getArtifactManager().prune( getArtifacts() );
		container.updateBootstrap();
	}

	/**
	 * Uninstalls all packages.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void uninstallPackages() throws SincerityException
	{
		getPackages().uninstall();
		getArtifactManager().prune();
		container.updateBootstrap();
	}

	/**
	 * Installs/upgrades dependencies.
	 * 
	 * @param overwrite
	 *        True to force overwrite of existing artifact files
	 * @param verify
	 *        Whether to verify the unpacking
	 * @throws SincerityException
	 *         In case of an error
	 */
	public abstract void install( boolean overwrite, boolean verify ) throws SincerityException;

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Prints the Sincerity installation disclaimer.
	 * 
	 * @param out
	 *        The print writer
	 */
	protected void printDisclaimer( PrintWriter out )
	{
		if( printedDisclaimer )
			return;

		out.println();
		out.println( "Sincerity has downloaded software from a repository and installed in your container." );
		out.println( "However, it is up to you to ensure that it is legal for you to use the installed software." );
		out.println( "Neither the developers of Sincerity nor the maintainers of the repositories can be held" );
		out.println( "legally responsible for your usage. In particular, note that most free and open source " );
		out.println( "software licenses grant you permission to use the software, without warranty, but place" );
		out.println( "limitations on your freedom to redistribute it." );
		out.println();
		out.println( "For your convenience, an effort has been made to provide you with access to the software" );
		out.println( "licenses. However, it is up to you to ensure that these are indeed the correct licenses for" );
		out.println( "each particular software product. Neither the developers of Sincerity nor the maintainers" );
		out.println( "of the repositories can be held legally responsible for the veracity of the information" );
		out.println( "regarding the licensing of particular software products. In particular, note that software" );
		out.println( "licenses may differ per version or edition of the software product, and that some software" );
		out.println( "is available under multiple licenses." );
		out.println();
		out.println( "Use the \"sincerity dependencies:licenses\" command to see a list of all licenses, or" );
		out.println( "\"sincerity shell:gui\" for a graphical interface." );
		out.println();

		printedDisclaimer = true;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File artifactsFile;

	private final Container<M, ?> container;

	private boolean printedDisclaimer;
}

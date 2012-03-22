/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity;

import com.threecrickets.sincerity.exception.SincerityException;

/**
 * Sincerity plugins handle the running of Sincerity commands (see
 * {@link Command}).
 * <p>
 * In order to allow for future upgrades to the plugin interface, while still
 * allowing support for older plugin implementation, this interface has a
 * version suffix.
 * <p>
 * This is version 1 of the plugin interface.
 * <p>
 * Note that although this is a Java language interface, a robust mechanism is
 * in place to allow you to delegate the interface to non-Java languages running
 * in the JVM, using the Scripturian library. See {@link DelegatedPlugin} for
 * more information.
 * 
 * @author Tal Liron
 * @see Plugins
 */
public interface Plugin1
{
	public int getVersion();

	public String getName();

	public String[] getCommands() throws SincerityException;

	public void run( Command command ) throws SincerityException;

	public void gui( Command command ) throws SincerityException;
}

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

package com.threecrickets.sincerity;

import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.CommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;
import com.threecrickets.sincerity.plugin.ShellPlugin;
import com.threecrickets.sincerity.plugin.swing.Frame;

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
	/**
	 * The version of the Sincerity plugin interface supported by this plugin.
	 * 
	 * @return The interface version number
	 * @throws SincerityException
	 *         In case of an error
	 */
	public int getInterfaceVersion() throws SincerityException;

	/**
	 * The name of this plugin. Plugin names are expected to be unique per
	 * container.
	 * 
	 * @return The plugin name
	 * @throws SincerityException
	 *         In case of an error
	 */
	public String getName() throws SincerityException;

	/**
	 * The command names supported by this plugin. Command names do not have to
	 * be unique per container, but unique names do make work easier for users.
	 * 
	 * @return An array of command names
	 * @throws SincerityException
	 *         In case of an error
	 */
	public String[] getCommands() throws SincerityException;

	/**
	 * Runs a command.
	 * <p>
	 * If the command is not supported by this plugin, should throw a
	 * {@link UnknownCommandException}. If the command does not have the
	 * arguments it needs in order to run, should throw a
	 * {@link BadArgumentsCommandException}. Other command-specific failures
	 * should throw a {@link CommandException}.
	 * 
	 * @param command
	 *        The command to run
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void run( Command command ) throws SincerityException;

	/**
	 * Called by the {@link ShellPlugin} to allow this plugin to activate its
	 * own extension to the GUI. This is an optional operation.
	 * <p>
	 * The {@link Frame} can be accessed at {@link Sincerity#getFrame()}. Note
	 * that when this hook is called, the frame is not yet made visible.
	 * 
	 * @param command
	 *        The command used to invoke the GUI (likely "shell:gui")
	 * @throws SincerityException
	 *         In case of an error
	 */
	public void gui( Command command ) throws SincerityException;
}

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.threecrickets.sincerity.exception.CommandException;
import com.threecrickets.sincerity.exception.NoContainerException;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * "Commands" are the core operation in Sincerity, and is implemented by a
 * plugin (see {@link Plugin1}). Each command can have any number of arguments.
 * <p>
 * Commands can be parsed similarly to command line interfaces: arguments can
 * include properties (such as "--prop=value") and switches (such as
 * "--switch").
 * 
 * @author Tal Liron
 * @see CommandException
 */
public class Command
{
	//
	// Constants
	//

	public static final String GREEDY_POSTFIX = "!";

	public static final int GREEDY_POSTFIX_LENGTH = GREEDY_POSTFIX.length();

	public static final String SWITCH_PREFIX = "--";

	public static final int SWITCH_PREFIX_LENGTH = SWITCH_PREFIX.length();

	public static final String PROPERTY_SEPARATOR = "=";

	public static final String COMMANDS_SEPARATOR = ":";

	public static final String PLUGIN_COMMAND_SEPARATOR = ":";

	public static final Command UNTIL = new Command( null, "UNTIL", false, null );

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param plugin
	 *        The plugin name, or null if not specified
	 * @param name
	 *        The command name
	 * @param isGreedy
	 *        True if greedy
	 * @param sincerity
	 *        The Sincerity instance
	 */
	public Command( String plugin, String name, boolean isGreedy, Sincerity sincerity )
	{
		this.plugin = plugin;
		this.name = name;
		this.isGreedy = isGreedy;
		this.sincerity = sincerity;
	}

	/**
	 * Constructor: supports parsing of the command.
	 * 
	 * @param name
	 *        The command name with an optional plugin prefix
	 * @param isGreedy
	 *        True if greedy
	 * @param sincerity
	 *        The Sincerity instance
	 */
	public Command( String name, boolean isGreedy, Sincerity sincerity )
	{
		String[] split = name.split( PLUGIN_COMMAND_SEPARATOR, 2 );
		if( split.length == 2 )
		{
			plugin = split[0];
			this.name = split[1];
		}
		else
			this.name = name;

		this.isGreedy = isGreedy;
		this.sincerity = sincerity;
	}

	//
	// Attributes
	//

	/**
	 * The command name.
	 * 
	 * @return The command name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * The Sincerity instance.
	 * 
	 * @return The Sincerity instance
	 */
	public Sincerity getSincerity()
	{
		return sincerity;
	}

	/**
	 * When {@link #setParse(boolean)} is false, returns the raw arguments of
	 * the command.
	 * <p>
	 * When {@link #setParse(boolean)} is true, returns only those arguments
	 * that are <i>not</i> switches or properties.
	 * 
	 * @return The arguments
	 * @throws SincerityException
	 *         In case of an error
	 */
	public String[] getArguments() throws SincerityException
	{
		if( arguments == null )
		{
			if( parse )
				parse();
			else
				arguments = rawArguments.toArray( new String[rawArguments.size()] );
		}
		return arguments;
	}

	/**
	 * Returns the command's switches: arguments prefixed with "--" that are not
	 * properties.
	 * <p>
	 * You must set {@link #setParse(boolean)} to true to support this method.
	 * 
	 * @return The switches or an empty set
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Set<String> getSwitches() throws SincerityException
	{
		if( switches == null )
		{
			if( parse )
				parse();
			else
				switches = Collections.emptySet();
		}
		return switches;
	}

	/**
	 * Returns the command's switches: arguments prefixed with "--" that have a
	 * "=" in their value.
	 * <p>
	 * You must set {@link #setParse(boolean)} to true to support this method.
	 * 
	 * @return The properties or an empty map
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Map<String, String> getProperties() throws SincerityException
	{
		if( properties == null )
		{
			if( parse )
				parse();
			else
				properties = Collections.emptyMap();
		}
		return properties;
	}

	/**
	 * @return Whether to parse
	 * @see #setParse(boolean)
	 */
	public boolean getParse()
	{
		return parse;
	}

	/**
	 * When true, supports {@link #getArguments()} in parsed mode,
	 * {@link #getSwitches()} and {@link #getProperties()}.
	 * 
	 * @param parse
	 *        True to support parsing
	 * @see #getParse()
	 */
	public void setParse( boolean parse )
	{
		this.parse = parse;
	}

	/**
	 * Creates command line arguments that could be parsed back into this
	 * command.
	 * 
	 * @return The command line arguments
	 */
	public String[] toArguments()
	{
		String[] arguments = new String[rawArguments.size() + 1];
		arguments[0] = toString();
		if( isGreedy )
			arguments[0] += GREEDY_POSTFIX;
		int i = 1;
		for( String argument : rawArguments )
			arguments[i++] = argument;
		return arguments;
	}

	//
	// Operations
	//

	/**
	 * Removes this command from the {@link Sincerity} instance's current
	 * command queue.
	 */
	public void remove()
	{
		sincerity.removeCommand( this );
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		if( plugin == null )
			return name;
		return plugin + PLUGIN_COMMAND_SEPARATOR + name;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The command's plugin, or null if not specified.
	 */
	protected String plugin;

	/**
	 * The original (raw) arguments of the command.
	 */
	protected final List<String> rawArguments = new ArrayList<String>();

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String name;

	private final boolean isGreedy;

	private final Sincerity sincerity;

	private boolean parse;

	private String[] arguments;

	private Set<String> switches;

	private Map<String, String> properties;

	/**
	 * Separates switches and properties from the rest of the arguments.
	 * 
	 * @throws SincerityException
	 *         In case of an error
	 */
	private void parse() throws SincerityException
	{
		ArrayList<String> arguments = new ArrayList<String>();
		switches = new HashSet<String>();
		properties = new HashMap<String, String>();

		for( String argument : rawArguments )
		{
			if( argument.startsWith( SWITCH_PREFIX ) )
			{
				argument = argument.substring( SWITCH_PREFIX_LENGTH );
				if( argument.length() > 0 )
					switches.add( argument );
			}
			else
			{
				try
				{
					sincerity.getContainer().getShortcuts().addArgument( argument, arguments );
				}
				catch( NoContainerException x )
				{
					arguments.add( argument );
				}
			}
		}

		for( String theSwitch : switches )
		{
			String[] split = theSwitch.split( PROPERTY_SEPARATOR, 2 );
			if( split.length == 2 )
				properties.put( split[0], split[1] );
		}

		this.arguments = arguments.toArray( new String[arguments.size()] );
	}
}

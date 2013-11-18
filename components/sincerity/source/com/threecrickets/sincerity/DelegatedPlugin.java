/**
 * Copyright 2011-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity;

import java.io.File;
import java.util.ArrayList;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.IoUtil;

/**
 * Allows you to delegate the {@link Plugin1} interface to non-Java languages
 * running in the JVM, using the Scripturian library.
 * 
 * @author Tal Liron
 * @see ScripturianShell
 */
public class DelegatedPlugin implements Plugin1
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param pluginFile
	 *        The Scripturian library file (also determines the plugin's default
	 *        name)
	 * @param shell
	 *        The shell
	 * @throws SincerityException
	 *         In case of an error
	 */
	public DelegatedPlugin( File pluginFile, ScripturianShell shell ) throws SincerityException
	{
		defaultName = IoUtil.separateExtensionFromFilename( pluginFile.getName() )[0];
		executable = shell.makeEnterable( "/" + shell.getContainer().getRelativePath( pluginFile ), ENTERING_KEY );
	}

	//
	// Plugin
	//

	public int getInterfaceVersion() throws SincerityException
	{
		try
		{
			Object version = executable.enter( ENTERING_KEY, "getInterfaceVersion" );
			if( version != null )
			{
				if( version instanceof Number )
					return ( (Number) version ).intValue();
				else
					return Integer.parseInt( version.toString() );
			}
		}
		catch( ParsingException x )
		{
			throw new SincerityException( "Could not parse source code for delegated plugin: " + defaultName, x );
		}
		catch( ExecutionException x )
		{
			if( x.getCause() instanceof SincerityException )
				throw (SincerityException) x.getCause();
			else
				throw new SincerityException( x.getCause() );
		}
		catch( NoSuchMethodException x )
		{
			// Optional method
		}
		return DEFAULT_VERSION;
	}

	public String getName() throws SincerityException
	{
		try
		{
			Object name = executable.enter( ENTERING_KEY, "getName" );
			if( name != null )
				return name.toString();
		}
		catch( ParsingException x )
		{
			throw new SincerityException( "Could not parse source code for delegated plugin: " + defaultName, x );
		}
		catch( ExecutionException x )
		{
			if( x.getCause() instanceof SincerityException )
				throw (SincerityException) x.getCause();
			else
				throw new SincerityException( x.getCause() );
		}
		catch( NoSuchMethodException x )
		{
			// Optional method
		}
		return defaultName;
	}

	public String[] getCommands() throws SincerityException
	{
		try
		{
			Object commands = executable.enter( ENTERING_KEY, "getCommands" );
			if( commands instanceof String[] )
				return (String[]) commands;
			else if( commands instanceof Iterable<?> )
			{
				ArrayList<String> commandList = new ArrayList<String>();
				for( Object command : (Iterable<?>) commands )
					commandList.add( command.toString() );
				return commandList.toArray( new String[commandList.size()] );
			}
		}
		catch( ParsingException x )
		{
			throw new SincerityException( "Could not parse source code for delegated plugin: " + defaultName, x );
		}
		catch( ExecutionException x )
		{
			if( x.getCause() instanceof SincerityException )
				throw (SincerityException) x.getCause();
			else
				throw new SincerityException( x.getCause() );
		}
		catch( NoSuchMethodException x )
		{
		}
		return null;
	}

	public void run( Command command ) throws SincerityException
	{
		try
		{
			executable.enter( ENTERING_KEY, "run", command );
		}
		catch( ParsingException x )
		{
			throw new SincerityException( "Could not parse source code for delegated plugin: " + defaultName, x );
		}
		catch( ExecutionException x )
		{
			if( x.getCause() instanceof SincerityException )
				throw (SincerityException) x.getCause();
			else
				throw new SincerityException( x.getCause() );
		}
		catch( NoSuchMethodException x )
		{
			throw new SincerityException( "Delegated plugin does not have a run() entry point: " + defaultName, x );
		}
	}

	public void gui( Command command ) throws SincerityException
	{
		try
		{
			executable.enter( ENTERING_KEY, "gui", command );
		}
		catch( ParsingException x )
		{
			throw new SincerityException( "Could not parse source code for delegated plugin: " + defaultName, x );
		}
		catch( ExecutionException x )
		{
			if( x.getCause() instanceof SincerityException )
				throw (SincerityException) x.getCause();
			else
				throw new SincerityException( x.getCause() );
		}
		catch( NoSuchMethodException x )
		{
			// Optional method
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String ENTERING_KEY = "sincerity";

	private static final int DEFAULT_VERSION = 0;

	private final String defaultName;

	private final Executable executable;
}

package com.threecrickets.sincerity;

import java.util.ArrayList;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.FileUtil;

public class DelegatedPlugin implements Plugin
{
	//
	// Construction
	//

	public DelegatedPlugin( String pluginFilename, ScripturianShell shell ) throws SincerityException
	{
		executable = shell.makeEnterable( pluginFilename, ENTERING_KEY );
		defaultName = FileUtil.separateExtensionFromFilename( pluginFilename )[0];
	}

	//
	// Plugin
	//

	public String getName()
	{
		try
		{
			Object name = executable.enter( ENTERING_KEY, "getName" );
			if( name != null )
				return name.toString();
		}
		catch( ParsingException x )
		{
		}
		catch( ExecutionException x )
		{
		}
		catch( NoSuchMethodException x )
		{
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
			throw new SincerityException( x.getMessage(), x.getCause() );
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
			throw new SincerityException( x.getMessage(), x.getCause() );
		}
		catch( NoSuchMethodException x )
		{
			throw new SincerityException( "Delegated plugin does not have a run() entry point: " + defaultName, x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String ENTERING_KEY = "sincerity";

	private final String defaultName;

	private final Executable executable;
}

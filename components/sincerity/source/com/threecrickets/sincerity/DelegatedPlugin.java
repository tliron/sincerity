package com.threecrickets.sincerity;

import java.io.File;
import java.util.ArrayList;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.FileUtil;

public class DelegatedPlugin implements Plugin1
{
	//
	// Construction
	//

	public DelegatedPlugin( File pluginFile, ScripturianShell shell ) throws SincerityException
	{
		defaultName = FileUtil.separateExtensionFromFilename( pluginFile.getName() )[0];
		executable = shell.makeEnterable( "/" + shell.getContainer().getRelativePath( pluginFile ), ENTERING_KEY );
	}

	//
	// Plugin
	//

	public int getVersion()
	{
		try
		{
			Object version = executable.enter( ENTERING_KEY, "getVersion" );
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
		}
		catch( ExecutionException x )
		{
		}
		catch( NoSuchMethodException x )
		{
		}
		return DEFAULT_VERSION;
	}

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
			throw new SincerityException( x.getMessage(), x.getCause() );
		}
		catch( NoSuchMethodException x )
		{
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String ENTERING_KEY = "sincerity";

	private static final int DEFAULT_VERSION = 1;

	private final String defaultName;

	private final Executable executable;
}

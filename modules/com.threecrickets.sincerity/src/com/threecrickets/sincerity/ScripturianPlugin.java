package com.threecrickets.sincerity;

import java.util.ArrayList;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

public class ScripturianPlugin implements Plugin
{
	//
	// Construction
	//

	public ScripturianPlugin( String pluginFile, ParsingContext parsingContext ) throws Exception
	{
		ExecutionContext executionContext = new ExecutionContext();
		boolean enterable = false;
		try
		{
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( pluginFile, false, parsingContext );
			Executable executable = documentDescriptor.getDocument();
			enterable = executable.makeEnterable( ENTERING_KEY, executionContext );
			if( enterable )
				this.executable = executable;
			else
				throw new Exception();
		}
		finally
		{
			if( !enterable )
				executionContext.release();
		}

		defaultName = pluginFile.split( "\\.(?=[^\\.]+$)", 2 )[0];
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

	public String[] getCommands()
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
		}
		catch( ExecutionException x )
		{
		}
		catch( NoSuchMethodException x )
		{
		}
		return null;
	}

	public void run( String command, String[] arguments, Sincerity sincerity ) throws Exception
	{
		try
		{
			executable.enter( ENTERING_KEY, "run", command, arguments, sincerity );
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
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String ENTERING_KEY = "sincerity";

	private final String defaultName;

	private final Executable executable;
}

package com.threecrickets.sincerity;

import java.io.IOException;
import java.util.ArrayList;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.FileUtil;
import com.threecrickets.sincerity.internal.SincerityExecutionController;

public class ScripturianPlugin implements Plugin
{
	//
	// Construction
	//

	public ScripturianPlugin( String pluginFilename, ParsingContext parsingContext, Sincerity sincerity ) throws SincerityException
	{
		ExecutionContext executionContext = new ExecutionContext();
		boolean enterable = false;
		try
		{
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( pluginFilename, false, parsingContext );
			Executable executable = documentDescriptor.getDocument();
			enterable = executable.makeEnterable( ENTERING_KEY, executionContext, null, new SincerityExecutionController( sincerity ) );
			if( enterable )
				this.executable = executable;
			else
				throw new SincerityException( "Tried to reenter Scripturian plugin: " + pluginFilename );
		}
		catch( ParsingException x )
		{
			throw new SincerityException( "Could not parse source code for Scripturian plugin: " + pluginFilename, x );
		}
		catch( DocumentException x )
		{
			throw new SincerityException( "Could not read source code for Scripturian plugin: " + pluginFilename, x );
		}
		catch( ExecutionException x )
		{
			throw new SincerityException( "Error executing Scripturian plugin: " + pluginFilename, x );
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not read source code for Scripturian plugin: " + pluginFilename, x );
		}
		finally
		{
			if( !enterable )
				executionContext.release();
		}

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
			throw new SincerityException( "Could not parse source code for Scripturian plugin: " + defaultName, x );
		}
		catch( ExecutionException x )
		{
			throw new SincerityException( "Error executing Scripturian plugin: " + defaultName, x );
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
			throw new SincerityException( "Could not parse source code for Scripturian plugin: " + defaultName, x );
		}
		catch( ExecutionException x )
		{
			throw new SincerityException( "Error executing Scripturian plugin: " + defaultName, x );
		}
		catch( NoSuchMethodException x )
		{
			throw new SincerityException( "Scripturian plugin does not have a run() entry point: " + defaultName, x );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String ENTERING_KEY = "sincerity";

	private final String defaultName;

	private final Executable executable;
}

package com.threecrickets.sincerity;

import java.util.ArrayList;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.sincerity.internal.FileUtil;
import com.threecrickets.sincerity.internal.SincerityExecutionController;

public class ScripturianPlugin implements Plugin
{
	//
	// Construction
	//

	public ScripturianPlugin( String pluginFilename, ParsingContext parsingContext, Sincerity sincerity ) throws Exception
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
				throw new Exception();
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

	public void run( Command command ) throws Exception
	{
		try
		{
			executable.enter( ENTERING_KEY, "run", command );
		}
		catch( ParsingException x )
		{
			throw x;
		}
		catch( ExecutionException x )
		{
			throw x;
		}
		catch( NoSuchMethodException x )
		{
			throw x;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String ENTERING_KEY = "sincerity";

	private final String defaultName;

	private final Executable executable;
}

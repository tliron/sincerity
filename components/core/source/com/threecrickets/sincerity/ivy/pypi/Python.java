package com.threecrickets.sincerity.ivy.pypi;

import java.io.File;
import java.io.IOException;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

public class Python
{
	//
	// Construction
	//

	public Python( String documentName, File baseDir )
	{
		this.documentName = documentName;
		this.baseDir = baseDir;
	}

	//
	// Operations
	//

	public Object call( String method, String... args ) throws ParsingException, DocumentException, ExecutionException, NoSuchMethodException, IOException
	{
		Executable executable = getExecutable();
		if( executable != null )
			return executable.enter( "sincerity", method, (Object[]) args );
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String documentName;

	private final File baseDir;

	private Executable executable = null;

	private synchronized Executable getExecutable() throws ParsingException, DocumentException, ExecutionException, IOException
	{
		if( this.executable == null )
		{
			LanguageManager languageManager = new LanguageManager();
			if( languageManager.getAdapterByTag( "python" ) == null )
				throw new RuntimeException( "Cannot find a Python language adapter" );

			DocumentFileSource<Executable> source = new DocumentFileSource<Executable>( baseDir, "default", "py", 1000 );
			ParsingContext parsingContext = new ParsingContext();
			parsingContext.setLanguageManager( languageManager );
			parsingContext.setDocumentSource( source );
			// parsingContext.setPrepare( true );
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( documentName, false, parsingContext );
			ExecutionContext executionContext = new ExecutionContext();
			boolean initialized = false;
			try
			{
				executionContext.getLibraryLocations().add( baseDir.toURI() );
				executionContext.getLibraryLocations().add( new File( "container/libraries/python" ).toURI() );
				Executable executable = documentDescriptor.getDocument();
				initialized = executable.makeEnterable( "sincerity", executionContext, null, null );
				if( initialized )
					this.executable = executable;
			}
			finally
			{
				if( !initialized )
					executionContext.release();
			}
		}
		return this.executable;
	}
}

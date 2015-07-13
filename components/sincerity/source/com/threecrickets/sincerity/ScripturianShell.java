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

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.Main;
import com.threecrickets.scripturian.ParserManager;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.ChainDocumentSource;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.parser.ProgramParser;
import com.threecrickets.scripturian.service.ApplicationService;
import com.threecrickets.scripturian.service.DocumentService;
import com.threecrickets.scripturian.service.Shell;
import com.threecrickets.sincerity.exception.ReenteringDocumentException;
import com.threecrickets.sincerity.exception.ScripturianException;
import com.threecrickets.sincerity.exception.SincerityException;

/**
 * An implementation of Scripturian's {@link Shell} interface for Sincerity
 * {@link Container} instances.
 * <p>
 * Additionally, includes utility methods to make it easy to invoke Scripturian
 * from Sincerity.
 * 
 * @author Tal Liron
 * @see DelegatedPlugin
 */
public class ScripturianShell implements Shell
{
	//
	// Construction
	//

	/**
	 * Initializes the parsing context for the shell.
	 * 
	 * @param container
	 *        The container
	 * @param prepare
	 *        True to prepare documents
	 * @param arguments
	 *        The arguments sent to {@link Main#main(String[])}
	 * @throws SincerityException
	 *         In case of an error
	 */
	public ScripturianShell( Container container, boolean prepare, String... arguments ) throws SincerityException
	{
		this.sincerity = container.getSincerity();
		this.arguments = arguments;

		DocumentFileSource<Executable> containerSource = new DocumentFileSource<Executable>( "container/", container.getRoot(), "default", "js", 1000 );
		DocumentFileSource<Executable> sinceritySource = new DocumentFileSource<Executable>( "sincerity/", this.sincerity.getHome(), "default", "js", 1000 );
		librarySources.add( new DocumentFileSource<Executable>( "container/libraries/scripturian/", container.getLibrariesFile( "scripturian" ), "default", "js", 1000 ) );
		librarySources.add( new DocumentFileSource<Executable>( "sincerity/libraries/scripturian/", this.sincerity.getHomeFile( "libraries", "scripturian" ), "default", "js", 1000 ) );

		ChainDocumentSource<Executable> source = new ChainDocumentSource<Executable>( "chain/" );
		source.getSources().add( containerSource );
		source.getSources().add( sinceritySource );

		parsingContext = new ParsingContext();
		parsingContext.setLanguageManager( container.getLanguageManager() );
		parsingContext.setParserManager( container.getParserManager() );
		parsingContext.setDocumentSource( source );
		parsingContext.setDefaultLanguageTag( "javascript" );
		parsingContext.setPrepare( prepare );
	}

	public ScripturianShell( Sincerity sincerity, boolean prepare, String... arguments ) throws SincerityException
	{
		this.sincerity = sincerity;
		this.arguments = arguments;

		DocumentFileSource<Executable> source = new DocumentFileSource<Executable>( "sincerity/", sincerity.getHome(), "default", "js", 1000 );
		librarySources.add( new DocumentFileSource<Executable>( "sincerity/libraries/scripturian/", sincerity.getHomeFile( "libraries", "scripturian" ), "default", "js", 1000 ) );

		ClassLoader classLoader = sincerity.getClass().getClassLoader();
		parsingContext = new ParsingContext();
		parsingContext.setLanguageManager( new LanguageManager( classLoader ) );
		parsingContext.setParserManager( new ParserManager( classLoader ) );
		parsingContext.setDocumentSource( source );
		parsingContext.setDefaultLanguageTag( "javascript" );
		parsingContext.setPrepare( prepare );
	}

	//
	// Operations
	//

	/**
	 * Creates a new execution context.
	 * 
	 * @return An execution context
	 */
	public ExecutionContext createExecutionContext()
	{
		ExecutionContext executionContext = new ExecutionContext( sincerity.getOut(), sincerity.getErr() );
		DocumentService documentService = new DocumentService( this, executionContext );
		documentService.setDefaultLanguageTag( parsingContext.getDefaultLanguageTag() );
		executionContext.getServices().put( "document", documentService );
		executionContext.getServices().put( "application", new ApplicationService( this ) );
		executionContext.getServices().put( "sincerity", sincerity );
		return executionContext;
	}

	/**
	 * Executes a document in a new execution context.
	 * 
	 * @param documentName
	 *        The document name
	 * @throws ScripturianException
	 *         In case of an error
	 */
	public void execute( String documentName ) throws ScripturianException
	{
		ExecutionContext executionContext = createExecutionContext();
		try
		{
			DocumentService documentService = (DocumentService) executionContext.getServices().get( "document" );
			try
			{
				documentService.execute( documentName );
			}
			catch( ParsingException x )
			{
				throw new ScripturianException( "Could not parse source code for execution: " + documentName, x );
			}
			catch( DocumentException x )
			{
				throw new ScripturianException( "Could not read source code for execution: " + documentName, x );
			}
			catch( ExecutionException x )
			{
				if( x.getCause() instanceof ScripturianException )
					throw (ScripturianException) x.getCause();
				else
					throw new ScripturianException( x.getCause() );
			}
			catch( IOException x )
			{
				throw new ScripturianException( "Could not read source code for execution: " + documentName, x );
			}
		}
		finally
		{
			executionContext.release();
		}
	}

	/**
	 * Makes the executable for the document enterable with a new execution
	 * context.
	 * 
	 * @param documentName
	 *        The document name
	 * @param enteringKey
	 *        The entering key
	 * @return The enterable executable
	 * @throws ScripturianException
	 *         In case of an error
	 */
	public Executable makeEnterable( String documentName, String enteringKey ) throws ScripturianException
	{
		boolean enterable = false;
		ExecutionContext executionContext = createExecutionContext();
		try
		{
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( documentName, ProgramParser.NAME, parsingContext );
			Executable executable = documentDescriptor.getDocument();
			DocumentService documentService = (DocumentService) executionContext.getServices().get( "document" );
			enterable = executable.makeEnterable( enteringKey, executionContext, documentService, null );
			if( enterable )
				return executable;
			else
				throw new ReenteringDocumentException( "Tried to reenter executable: " + documentName );
		}
		catch( ParsingException x )
		{
			throw new ScripturianException( "Could not parse source code for execution: " + documentName, x );
		}
		catch( DocumentException x )
		{
			throw new ScripturianException( "Could not read source code for execution: " + documentName, x );
		}
		catch( ExecutionException x )
		{
			if( x.getCause() instanceof ScripturianException )
				throw (ScripturianException) x.getCause();
			else
				throw new ScripturianException( x.getCause() );
		}
		catch( IOException x )
		{
			throw new ScripturianException( "Could not read source code for execution: " + documentName, x );
		}
		finally
		{
			if( !enterable )
				executionContext.release();
		}
	}

	//
	// Shell
	//

	public Logger getLogger()
	{
		return logger;
	}

	public void setLogger( Logger logger )
	{
		this.logger = logger;
	}

	public String[] getArguments()
	{
		return arguments;
	}

	public LanguageManager getLanguageManager()
	{
		return parsingContext.getLanguageManager();
	}

	public ParserManager getParserManager()
	{
		return parsingContext.getParserManager();
	}

	public boolean isPrepare()
	{
		return parsingContext.isPrepare();
	}

	public DocumentSource<Executable> getSource()
	{
		return parsingContext.getDocumentSource();
	}

	public CopyOnWriteArrayList<DocumentSource<Executable>> getLibrarySources()
	{
		return librarySources;
	}

	public ExecutionController getExecutionController()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Sincerity sincerity;

	private final String[] arguments;

	private final ParsingContext parsingContext;

	private final CopyOnWriteArrayList<DocumentSource<Executable>> librarySources = new CopyOnWriteArrayList<DocumentSource<Executable>>();

	private volatile Logger logger = Logger.getLogger( "sincerity" );
}

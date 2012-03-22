/**
 * Copyright 2011-2012 Three Crickets LLC.
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
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.service.ApplicationService;
import com.threecrickets.scripturian.service.DocumentService;
import com.threecrickets.scripturian.service.Shell;
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

	public ScripturianShell( Container container, File sourceDir, boolean prepare, String... arguments ) throws SincerityException
	{
		this.container = container;
		languageManager = container.getLanguageManager();
		if( sourceDir == null )
			sourceDir = container.getRoot();
		source = new DocumentFileSource<Executable>( "container/", sourceDir, "default", "js", 1000 );
		librarySources.add( new DocumentFileSource<Executable>( "container/libraries/scripturian/", container.getLibrariesFile( "scripturian" ), "default", "js", -1 ) );
		librarySources.add( new DocumentFileSource<Executable>( "sincerity/", container.getSincerity().getHomeFile( "libraries", "scripturian" ), "default", "js", -1 ) );
		this.prepare = prepare;
		this.arguments = arguments;
	}

	//
	// Operations
	//

	public ExecutionContext createExecutionContext()
	{
		ExecutionContext executionContext = new ExecutionContext( container.getSincerity().getOut(), container.getSincerity().getErr() );
		DocumentService documentService = new DocumentService( this, executionContext );
		documentService.setDefaultLanguageTag( "javascript" );
		executionContext.getServices().put( "document", documentService );
		executionContext.getServices().put( "application", new ApplicationService( this ) );
		executionContext.getServices().put( "sincerity", container.getSincerity() );
		return executionContext;
	}

	public void execute( String documentName ) throws SincerityException
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
				throw new SincerityException( "Could not parse source code for execution: " + documentName, x );
			}
			catch( DocumentException x )
			{
				throw new SincerityException( "Could not read source code for execution: " + documentName, x );
			}
			catch( ExecutionException x )
			{
				throw new SincerityException( x.getMessage(), x.getCause() );
			}
			catch( IOException x )
			{
				throw new SincerityException( "Could not read source code for execution: " + documentName, x );
			}
		}
		finally
		{
			executionContext.release();
		}
	}

	public Executable makeEnterable( String documentName, String enteringKey ) throws SincerityException
	{
		ParsingContext parsingContext = new ParsingContext();
		parsingContext.setLanguageManager( languageManager );
		parsingContext.setDocumentSource( source );
		parsingContext.setDefaultLanguageTag( "javascript" );
		parsingContext.setPrepare( prepare );

		boolean enterable = false;
		ExecutionContext executionContext = createExecutionContext();
		try
		{
			DocumentDescriptor<Executable> documentDescriptor = Executable.createOnce( documentName, false, parsingContext );
			Executable executable = documentDescriptor.getDocument();
			enterable = executable.makeEnterable( enteringKey, executionContext );
			if( enterable )
				return executable;
			else
				throw new SincerityException( "Tried to reenter executable: " + documentName );
		}
		catch( ParsingException x )
		{
			throw new SincerityException( "Could not parse source code for execution: " + documentName, x );
		}
		catch( DocumentException x )
		{
			throw new SincerityException( "Could not read source code for execution: " + documentName, x );
		}
		catch( ExecutionException x )
		{
			throw new SincerityException( x.getMessage(), x.getCause() );
		}
		catch( IOException x )
		{
			throw new SincerityException( "Could not read source code for execution: " + documentName, x );
		}
		finally
		{
			if( !enterable )
				executionContext.release();
		}
	}

	//
	// Attributes
	//

	public Container getContainer()
	{
		return container;
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
		return languageManager;
	}

	public boolean isPrepare()
	{
		return prepare;
	}

	public DocumentSource<Executable> getSource()
	{
		return source;
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

	private final Container container;

	private final String[] arguments;

	private final LanguageManager languageManager;

	private final boolean prepare;

	private final DocumentSource<Executable> source;

	private final CopyOnWriteArrayList<DocumentSource<Executable>> librarySources = new CopyOnWriteArrayList<DocumentSource<Executable>>();

	private volatile Logger logger = Logger.getLogger( "sincerity" );
}

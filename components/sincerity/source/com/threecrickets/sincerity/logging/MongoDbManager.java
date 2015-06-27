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

package com.threecrickets.sincerity.logging;

import java.util.Date;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext.ContextStack;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseManager;
import org.apache.logging.log4j.message.Message;
import org.bson.Document;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * A Log4j database manager for MongoDB.
 * <p>
 * <b>Warning:</b> Because the MongoDB driver itself emits log messages, you
 * might cause recursion here that would lead to hangs and timeouts. The easiest
 * solution is to simply disable its logging via your Log4j configuration: just
 * set the <code>org.mongodb.driver</code> logger to level {@link Level#OFF}.
 * 
 * @author Tal Liron
 */
public class MongoDbManager extends AbstractDatabaseManager
{
	//
	// Static operations
	//

	/**
	 * Creates a MongoDB manager for use within the {@link MongoDbAppender}, or
	 * returns a suitable one if it already exists.
	 * 
	 * @param name
	 *        The name of the manager, which should include connection details
	 *        and hashed passwords where possible.
	 * @param bufferSize
	 *        The size of the log event buffer.
	 * @param uri
	 *        The MongoDB URI (see {@link MongoClientURI}) (not used if "client"
	 *        is specified)
	 * @param client
	 *        The MongoDB client (not used if "uri" is specified)
	 * @param dbName
	 *        The MongoDB database name
	 * @param collectionName
	 *        The MongoDB collection name
	 * @param writeConcernName
	 *        The MongoDB write concern (see
	 *        {@link WriteConcern#valueOf(String)})
	 * @return a new or existing MongoDB manager as applicable.
	 */
	public static MongoDbManager getMongoDbManager( String name, int bufferSize, String uri, MongoClient client, String dbName, String collectionName, String writeConcernName )
	{
		return AbstractDatabaseManager.getManager( name, new FactoryData( bufferSize, uri, client, dbName, collectionName, writeConcernName ), FACTORY );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Construction
	//

	protected MongoDbManager( String name, int bufferSize, String uri, MongoClient client, String databaseName, String collectionName, String writeConcernName )
	{
		super( name, bufferSize );
		this.uri = uri;
		this.client = client;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
		this.writeConcernName = writeConcernName;
	}

	//
	// AbstractDatabaseManager
	//

	@Override
	protected void startupInternal() throws Exception
	{
		if( collection != null )
			return;

		client = null;
		database = null;
		collection = null;

		MongoClientURI uri;
		try
		{
			if( this.uri == null )
				uri = new MongoClientURI( this.uri );
			else
				uri = new MongoClientURI( "mongodb://localhost:27017/" );
		}
		catch( IllegalArgumentException x )
		{
			throw new AppenderLoggingException( "Can't parse MongoDB uri: " + this.uri );
		}

		try
		{
			client = new MongoClient( uri );
		}
		catch( MongoException x )
		{
			throw new AppenderLoggingException( "Can't create MongoDB client: " + uri );
		}

		try
		{
			database = client.getDatabase( databaseName );
		}
		catch( MongoException x )
		{
			client.close();
			client = null;
			throw new AppenderLoggingException( "Can't access MongoDB database: " + databaseName );
		}

		try
		{
			collection = database.getCollection( collectionName );
		}
		catch( MongoException x )
		{
			client.close();
			client = null;
			database = null;
			throw new AppenderLoggingException( "Can't access MongoDB collection: " + collectionName );
		}

		if( writeConcernName != null )
		{
			WriteConcern writeConcern = WriteConcern.valueOf( writeConcernName );
			if( writeConcern == null )
				throw new AppenderLoggingException( "Unsupported MongoDB write concern: " + writeConcernName );
			collection = collection.withWriteConcern( writeConcern );
		}
	}

	@Override
	protected void shutdownInternal() throws Exception
	{
		if( client != null )
			client.close();
		client = null;
		database = null;
		collection = null;
	}

	@Override
	protected void connectAndStart()
	{
	}

	@Override
	protected void writeInternal( LogEvent event )
	{
		if( collection == null )
			throw new AppenderLoggingException( "Not connected to MongoDB" );

		Document o = new Document();

		o.put( "timestamp", new Date( event.getTimeMillis() ) );
		o.put( "logger", event.getLoggerName() );

		Level level = event.getLevel();
		if( level != null )
			o.put( "level", level.name() );

		Marker marker = event.getMarker();
		if( marker != null )
			o.put( "marker", marker.getName() );

		Message message = event.getMessage();
		if( message != null )
			o.put( "message", message.getFormattedMessage() );

		StackTraceElement eventSource = event.getSource();
		if( eventSource != null )
		{
			Document source = new Document();
			source.put( "class", eventSource.getClassName() );
			source.put( "method", eventSource.getMethodName() );
			source.put( "file", eventSource.getFileName() );
			source.put( "line", eventSource.getLineNumber() );
			o.put( "source", source );
		}

		Document thread = new Document();

		thread.put( "name", event.getThreadName() );

		Map<String, String> eventContextMap = event.getContextMap();
		if( eventContextMap != null )
		{
			Document contextMap = new Document();
			for( Map.Entry<String, String> entry : eventContextMap.entrySet() )
				contextMap.put( entry.getKey(), entry.getValue() );
			thread.put( "contextMap", contextMap );
		}

		ContextStack eventContextStack = event.getContextStack();
		if( eventContextStack != null )
		{
			BasicDBList contextStack = new BasicDBList();
			for( String entry : eventContextStack )
				contextStack.add( entry );
			thread.put( "contextStack", contextStack );
		}

		o.put( "thread", thread );

		try
		{
			collection.insertOne( o );
		}
		catch( MongoException e )
		{
			throw new AppenderLoggingException( "Can't write to MongoDB", e );
		}
	}

	@Override
	protected void commitAndClose()
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final MongoDbManagerFactory FACTORY = new MongoDbManagerFactory();

	private final String uri;

	private final String databaseName;

	private final String collectionName;

	private final String writeConcernName;

	private MongoClient client;

	private MongoDatabase database;

	private MongoCollection<Document> collection;

	/**
	 * Creates managers.
	 */
	private static final class MongoDbManagerFactory implements ManagerFactory<MongoDbManager, FactoryData>
	{
		@Override
		public MongoDbManager createManager( final String name, final FactoryData data )
		{
			return new MongoDbManager( name, data.getBufferSize(), data.uri, data.client, data.dbName, data.collectionName, data.writeConcernName );
		}
	}

	/**
	 * Encapsulates data that {@link MongoDbManagerFactory} uses to create
	 * managers.
	 */
	private static final class FactoryData extends AbstractDatabaseManager.AbstractFactoryData
	{
		protected FactoryData( final int bufferSize, String uri, MongoClient client, String dbName, String collectionName, String writeConcernName )
		{
			super( bufferSize );
			this.uri = uri;
			this.client = client;
			this.dbName = dbName;
			this.collectionName = collectionName;
			this.writeConcernName = writeConcernName;
		}

		private final String uri;

		private final MongoClient client;

		private final String dbName;

		private final String collectionName;

		private final String writeConcernName;
	}
}

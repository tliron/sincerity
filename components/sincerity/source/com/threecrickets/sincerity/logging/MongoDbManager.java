/**
 * Copyright 2011-2014 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.logging;

import java.net.UnknownHostException;
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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

/**
 * A Log4j database manager for MongoDB.
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
	 *        TODO
	 * @param client
	 *        TODO
	 * @param dbName
	 *        TODO
	 * @param collectionName
	 *        TODO
	 * @return a new or existing MongoDB manager as applicable.
	 */
	public static MongoDbManager getMongoDbManager( String name, int bufferSize, MongoClientURI uri, MongoClient client, String dbName, String collectionName )
	{
		return AbstractDatabaseManager.getManager( name, new FactoryData( bufferSize, uri, client, dbName, collectionName ), FACTORY );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Construction
	//

	protected MongoDbManager( String name, int bufferSize, MongoClientURI uri, MongoClient client, String db, String collectionName )
	{
		super( name, bufferSize );
		this.uri = uri;
		this.client = client;
		this.dbName = db;
		this.collectionName = collectionName;
	}

	//
	// AbstractDatabaseManager
	//

	@Override
	protected void startupInternal() throws Exception
	{
		MongoClientURI uri = this.uri;
		if( uri == null )
			uri = new MongoClientURI( "mongodb://localhost:27017/" );

		try
		{
			client = new MongoClient( uri );
		}
		catch( UnknownHostException e )
		{
			throw new AppenderLoggingException( "Can't connect to MongoDB: " + uri, e );
		}

		db = client.getDB( dbName );
		if( db == null )
		{
			client.close();
			client = null;
			throw new AppenderLoggingException( "Can't access MongoDB database: " + dbName );
		}

		collection = db.getCollection( collectionName );
		if( collection == null )
		{
			client.close();
			client = null;
			db = null;
			throw new AppenderLoggingException( "Can't access MongoDB collection: " + collectionName );
		}
	}

	@Override
	protected void shutdownInternal() throws Exception
	{
		if( client != null )
			client.close();
		client = null;
		db = null;
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

		BasicDBObject o = new BasicDBObject();

		o.put( "timestamp", new Date( event.getTimeMillis() ) );
		o.put( "logger", event.getLoggerName() );

		Level level = event.getLevel();
		if( level != null )
			o.put( "level", level.name() );

		Message message = event.getMessage();
		if( message != null )
			o.put( "message", message.getFormattedMessage() );

		o.put( "thread", event.getThreadName() );

		StackTraceElement eventSource = event.getSource();
		if( eventSource != null )
		{
			BasicDBObject source = new BasicDBObject();
			source.put( "class", eventSource.getClassName() );
			source.put( "method", eventSource.getMethodName() );
			source.put( "file", eventSource.getFileName() );
			source.put( "line", eventSource.getLineNumber() );
			o.put( "source", source );
		}

		Map<String, String> eventContext = event.getContextMap();
		if( eventContext != null )
		{
			BasicDBObject context = new BasicDBObject();
			for( Map.Entry<String, String> entry : eventContext.entrySet() )
				context.put( entry.getKey(), entry.getValue() );
			o.put( "context", context );
		}

		ContextStack eventStack = event.getContextStack();
		if( eventStack != null )
		{
			BasicDBList stack = new BasicDBList();
			for( String entry : eventStack )
				stack.add( entry );
			o.put( "stack", stack );
		}

		Marker marker = event.getMarker();
		if( marker != null )
			o.put( "marker", marker.getName() );

		try
		{
			collection.save( o );
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

	private final MongoClientURI uri;

	private final String dbName;

	private final String collectionName;

	private MongoClient client;

	private DB db;

	private DBCollection collection;

	/**
	 * Encapsulates data that {@link MongoDbManagerFactory} uses to create
	 * managers.
	 */
	private static final class FactoryData extends AbstractDatabaseManager.AbstractFactoryData
	{
		protected FactoryData( final int bufferSize, MongoClientURI uri, MongoClient client, String dbName, String collectionName )
		{
			super( bufferSize );
			this.uri = uri;
			this.client = client;
			this.dbName = dbName;
			this.collectionName = collectionName;
		}

		private final MongoClientURI uri;

		private final MongoClient client;

		private final String dbName;

		private final String collectionName;
	}

	/**
	 * Creates managers.
	 */
	private static final class MongoDbManagerFactory implements ManagerFactory<MongoDbManager, FactoryData>
	{
		@Override
		public MongoDbManager createManager( final String name, final FactoryData data )
		{
			return new MongoDbManager( name, data.getBufferSize(), data.uri, data.client, data.dbName, data.collectionName );
		}
	}
}
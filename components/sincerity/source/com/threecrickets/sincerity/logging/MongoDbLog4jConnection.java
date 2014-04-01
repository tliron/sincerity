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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.appender.db.nosql.NoSQLConnection;
import org.apache.logging.log4j.core.appender.db.nosql.NoSQLObject;
import org.apache.logging.log4j.core.appender.db.nosql.mongodb.MongoDBObject;
import org.apache.logging.log4j.status.StatusLogger;
import org.bson.BSON;
import org.bson.Transformer;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * Temporary fix for <a
 * href="https://issues.apache.org/jira/browse/LOG4J2-591">LOG4J2-591</a>.
 * 
 * @author Tal Liron
 */
public final class MongoDbLog4jConnection implements NoSQLConnection<BasicDBObject, MongoDBObject>
{
	private static final Logger LOGGER = StatusLogger.getLogger();

	static
	{
		BSON.addEncodingHook( Level.class, new Transformer()
		{
			@Override
			public Object transform( Object o )
			{
				if( o instanceof Level )
				{
					return ( (Level) o ).name();
				}
				return o;
			}
		} );
	}

	private final DBCollection collection;

	private final Mongo mongo;

	private final WriteConcern writeConcern;

	public MongoDbLog4jConnection( final DB database, final WriteConcern writeConcern, final String collectionName )
	{
		this.mongo = database.getMongo();
		this.collection = database.getCollection( collectionName );
		this.writeConcern = writeConcern;
	}

	@Override
	public MongoDBObject createObject()
	{
		return new MongoDBObject();
	}

	@Override
	public MongoDBObject[] createList( final int length )
	{
		return new MongoDBObject[length];
	}

	@Override
	public void insertObject( final NoSQLObject<BasicDBObject> object )
	{
		try
		{
			final WriteResult result = this.collection.insert( object.unwrap(), this.writeConcern );
			if( result.getError() != null && result.getError().length() > 0 )
			{
				throw new AppenderLoggingException( "Failed to write log event to MongoDB due to error: " + result.getError() + "." );
			}
		}
		catch( final MongoException e )
		{
			throw new AppenderLoggingException( "Failed to write log event to MongoDB due to error: " + e.getMessage(), e );
		}
	}

	@Override
	public void close()
	{
		// See: https://issues.apache.org/jira/browse/LOG4J2-591
		// this.mongo.close();
	}

	@Override
	public boolean isClosed()
	{
		return !this.mongo.getConnector().isOpen();
	}

	/**
	 * To prevent class loading issues during plugin discovery, this code cannot
	 * live within MongoDBProvider. This is because of how Java treats
	 * references to Exception classes different from references to other
	 * classes. When Java loads a class, it normally won't load that class's
	 * dependent classes until and unless A) they are used, B) the class being
	 * loaded extends or implements those classes, or C) those classes are the
	 * types of static members in the class. However, exceptions that a class
	 * uses are always loaded when the class is loaded, even before they are
	 * actually used.
	 * 
	 * @param database
	 *        The database to authenticate
	 * @param username
	 *        The username to authenticate with
	 * @param password
	 *        The password to authenticate with
	 */
	static void authenticate( final DB database, final String username, final String password )
	{
		try
		{
			if( !database.authenticate( username, password.toCharArray() ) )
			{
				LOGGER.error( "Failed to authenticate against MongoDB server. Unknown error." );
			}
		}
		catch( final MongoException e )
		{
			LOGGER.error( "Failed to authenticate against MongoDB: " + e.getMessage(), e );
		}
		catch( final IllegalStateException e )
		{
			LOGGER.error( "Factory-supplied MongoDB database connection already authenticated with different" + "credentials but lost connection." );
		}
	}
}

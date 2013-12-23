/**
 * Copyright 2011-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.logging;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.db.nosql.NoSQLProvider;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.helpers.NameUtil;
import org.apache.logging.log4j.status.StatusLogger;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

/**
 * Temporary fix for <a
 * href="https://issues.apache.org/jira/browse/LOG4J2-474">LOG4J2-474</a>.
 * 
 * @author Tal Liron
 */
public class MongoDbLog4jProvider implements NoSQLProvider<MongoDbLog4jConnection>
{
	private static final Logger LOGGER = StatusLogger.getLogger();

	private final String collectionName;

	private final DB database;

	private final String description;

	private final WriteConcern writeConcern;

	private MongoDbLog4jProvider( final DB database, final WriteConcern writeConcern, final String collectionName, final String description )
	{
		this.database = database;
		this.writeConcern = writeConcern;
		this.collectionName = collectionName;
		this.description = "mongoDb{ " + description + " }";
	}

	@Override
	public MongoDbLog4jConnection getConnection()
	{
		return new MongoDbLog4jConnection( this.database, this.writeConcern, this.collectionName );
	}

	@Override
	public String toString()
	{
		return this.description;
	}

	/**
	 * Factory method for creating a MongoDB provider within the plugin manager.
	 * 
	 * @param collectionName
	 *        The name of the MongoDB collection to which log events should be
	 *        written.
	 * @param writeConcernConstant
	 *        The {@link WriteConcern} constant to control writing details,
	 *        defaults to {@link WriteConcern#ACKNOWLEDGED}.
	 * @param writeConcernConstantClassName
	 *        The name of a class containing the aforementioned static
	 *        WriteConcern constant. Defaults to {@link WriteConcern}.
	 * @param databaseName
	 *        The name of the MongoDB database containing the collection to
	 *        which log events should be written. Mutually exclusive with
	 *        {@code factoryClassName&factoryMethodName!=null}.
	 * @param server
	 *        The host name of the MongoDB server, defaults to localhost and
	 *        mutually exclusive with
	 *        {@code factoryClassName&factoryMethodName!=null}.
	 * @param port
	 *        The port the MongoDB server is listening on, defaults to the
	 *        default MongoDB port and mutually exclusive with
	 *        {@code factoryClassName&factoryMethodName!=null}.
	 * @param username
	 *        The username to authenticate against the MongoDB server with.
	 * @param password
	 *        The password to authenticate against the MongoDB server with.
	 * @param factoryClassName
	 *        A fully qualified class name containing a static factory method
	 *        capable of returning a {@link DB} or a {@link MongoClient}.
	 * @param factoryMethodName
	 *        The name of the public static factory method belonging to the
	 *        aforementioned factory class.
	 * @return a new MongoDB provider.
	 */
	@PluginFactory
	public static MongoDbLog4jProvider createNoSQLProvider( final String collectionName, final String writeConcernConstant, final String writeConcernConstantClassName, final String databaseName, final String server,
		final String port, final String username, final String password, final String factoryClassName, final String factoryMethodName )
	{
		DB database;
		String description;
		if( factoryClassName != null && factoryClassName.length() > 0 && factoryMethodName != null && factoryMethodName.length() > 0 )
		{
			try
			{
				final Class<?> factoryClass = Class.forName( factoryClassName );
				final Method method = factoryClass.getMethod( factoryMethodName );
				final Object object = method.invoke( null );

				if( object instanceof DB )
				{
					database = (DB) object;
				}
				else if( object instanceof MongoClient )
				{
					if( databaseName != null && databaseName.length() > 0 )
					{
						database = ( (MongoClient) object ).getDB( databaseName );
					}
					else
					{
						LOGGER.error( "The factory method [{}.{}()] returned a MongoClient so the database name is " + "required.", factoryClassName, factoryMethodName );
						return null;
					}
				}
				else if( object == null )
				{
					LOGGER.error( "The factory method [{}.{}()] returned null.", factoryClassName, factoryMethodName );
					return null;
				}
				else
				{
					LOGGER.error( "The factory method [{}.{}()] returned an unsupported type [{}].", factoryClassName, factoryMethodName, object.getClass().getName() );
					return null;
				}

				description = "database=" + database.getName();
				final List<ServerAddress> addresses = database.getMongo().getAllAddress();
				if( addresses.size() == 1 )
				{
					description += ", server=" + addresses.get( 0 ).getHost() + ", port=" + addresses.get( 0 ).getPort();
				}
				else
				{
					description += ", servers=[";
					for( final ServerAddress address : addresses )
					{
						description += " { " + address.getHost() + ", " + address.getPort() + " } ";
					}
					description += "]";
				}
			}
			catch( final ClassNotFoundException e )
			{
				LOGGER.error( "The factory class [{}] could not be loaded.", factoryClassName, e );
				return null;
			}
			catch( final NoSuchMethodException e )
			{
				LOGGER.error( "The factory class [{}] does not have a no-arg method named [{}].", factoryClassName, factoryMethodName, e );
				return null;
			}
			catch( final Exception e )
			{
				LOGGER.error( "The factory method [{}.{}()] could not be invoked.", factoryClassName, factoryMethodName, e );
				return null;
			}
		}
		else if( databaseName != null && databaseName.length() > 0 )
		{
			description = "database=" + databaseName;
			try
			{
				if( server != null && server.length() > 0 )
				{
					final int portInt = AbstractAppender.parseInt( port, 0 );
					description += ", server=" + server;
					if( portInt > 0 )
					{
						description += ", port=" + portInt;
						database = new MongoClient( server, portInt ).getDB( databaseName );
					}
					else
					{
						database = new MongoClient( server ).getDB( databaseName );
					}
				}
				else
				{
					database = new MongoClient().getDB( databaseName );
				}
			}
			catch( final Exception e )
			{
				LOGGER.error( "Failed to obtain a database instance from the MongoClient at server [{}] and " + "port [{}].", server, port );
				return null;
			}
		}
		else
		{
			LOGGER.error( "No factory method was provided so the database name is required." );
			return null;
		}

		// if( !database.isAuthenticated() )
		{
			if( username != null && username.length() > 0 && password != null && password.length() > 0 )
			{
				description += ", username=" + username + ", passwordHash=" + NameUtil.md5( password + MongoDbLog4jProvider.class.getName() );
				MongoDbLog4jConnection.authenticate( database, username, password );
			}
			/*
			 * else { LOGGER.error(
			 * "The database is not already authenticated so you must supply a username and password "
			 * + "for the MongoDB provider." ); return null; }
			 */
		}

		WriteConcern writeConcern;
		if( writeConcernConstant != null && writeConcernConstant.length() > 0 )
		{
			if( writeConcernConstantClassName != null && writeConcernConstantClassName.length() > 0 )
			{
				try
				{
					final Class<?> writeConcernConstantClass = Class.forName( writeConcernConstantClassName );
					final Field field = writeConcernConstantClass.getField( writeConcernConstant );
					writeConcern = (WriteConcern) field.get( null );
				}
				catch( final Exception e )
				{
					LOGGER.error( "Write concern constant [{}.{}] not found, using default.", writeConcernConstantClassName, writeConcernConstant );
					writeConcern = WriteConcern.ACKNOWLEDGED;
				}
			}
			else
			{
				writeConcern = WriteConcern.valueOf( writeConcernConstant );
				if( writeConcern == null )
				{
					LOGGER.warn( "Write concern constant [{}] not found, using default.", writeConcernConstant );
					writeConcern = WriteConcern.ACKNOWLEDGED;
				}
			}
		}
		else
		{
			writeConcern = WriteConcern.ACKNOWLEDGED;
		}

		return new MongoDbLog4jProvider( database, writeConcern, collectionName, description );
	}
}

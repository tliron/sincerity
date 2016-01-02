/**
 * Copyright 2011-2016 Three Crickets LLC.
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
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.db.AbstractDatabaseAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.util.Booleans;

import com.mongodb.MongoClientURI;
import com.mongodb.WriteConcern;

/**
 * A Log4j appender for MongoDB.
 * <p>
 * <b>Warning:</b> Because the MongoDB driver itself emits log messages, you
 * might cause recursion here that would lead to hangs and timeouts. The easiest
 * solution is to simply disable its logging via your Log4j configuration: just
 * set the <code>org.mongodb.driver</code> logger to level {@link Level#OFF}.
 * 
 * @author Tal Liron
 */
@Plugin(name = "MongoDB", category = "Sincerity", elementType = "appender", printObject = true)
public class MongoDbAppender extends AbstractDatabaseAppender<MongoDbManager>
{
	//
	// Static operations
	//

	/**
	 * Factory method for creating a MongoDB appender within the plugin manager.
	 * 
	 * @param name
	 *        The name of the appender.
	 * @param ignore
	 *        If {@code "true"} (default) exceptions encountered when appending
	 *        events are logged; otherwise they are propagated to the caller.
	 * @param filter
	 *        The filter, if any, to use.
	 * @param bufferSize
	 *        If an integer greater than 0, this causes the appender to buffer
	 *        log events and flush whenever the buffer reaches this size.
	 * @param uri
	 *        The MongoDB URI (see {@link MongoClientURI})
	 * @param db
	 *        The MongoDB database name
	 * @param collection
	 *        The MongoDB collection name
	 * @param writeConcern
	 *        The MongoDB write concern (see
	 *        {@link WriteConcern#valueOf(String)})
	 * @return a new JDBC appender.
	 */
	@PluginFactory
	public static MongoDbAppender createAppender( @PluginAttribute("name" ) final String name, @PluginAttribute("ignoreExceptions") final String ignore, @PluginElement("Filter") final Filter filter,
		@PluginAttribute("bufferSize") final String bufferSize, @PluginAttribute("uri") final String uri, @PluginAttribute("db") final String db, @PluginAttribute("collection") final String collection,
		@PluginAttribute("writeConcern") final String writeConcern)
	{
		int bufferSizeInt = AbstractAppender.parseInt( bufferSize, 0 );
		boolean ignoreExceptions = Booleans.parseBoolean( ignore, true );

		StringBuilder managerName = new StringBuilder( "mongoDbManager{ description=" ).append( name ).append( ", bufferSize=" ).append( bufferSizeInt ).append( ", uri=" ).append( uri ).append( ", db=" ).append( db )
			.append( ", collection=" ).append( collection ).append( ", writeConcern=" ).append( writeConcern ).append( " }" );

		MongoDbManager manager = MongoDbManager.getMongoDbManager( managerName.toString(), bufferSizeInt, uri, null, db, collection, writeConcern );
		if( manager == null )
			return null;

		return new MongoDbAppender( name, filter, ignoreExceptions, manager );
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return description;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Construction
	//

	protected MongoDbAppender( String name, Filter filter, boolean ignoreExceptions, MongoDbManager manager )
	{
		super( name, filter, ignoreExceptions, manager );
		description = getName() + "{ manager=" + getManager() + " }";
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final String description;
}

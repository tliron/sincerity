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

import com.mongodb.DB;
import com.mongodb.MongoClient;

/**
 * @author Tal Liron
 */
public class MongoDbFactory
{
	//
	// Static attributes
	//

	public static MongoClient getClient()
	{
		return client;
	}

	public static void setClient( MongoClient client )
	{
		MongoDbFactory.client = client;
	}

	public static DB getDB()
	{
		return db;
	}

	public static void setDB( DB db )
	{
		MongoDbFactory.db = db;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	public static MongoClient client;

	public static DB db;
}

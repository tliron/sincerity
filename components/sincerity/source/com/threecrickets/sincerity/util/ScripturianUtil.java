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

package com.threecrickets.sincerity.util;

import com.threecrickets.scripturian.LanguageManager;

/**
 * Scripturian utilities.
 * 
 * @author Tal Liron
 */
public abstract class ScripturianUtil
{
	//
	// Static operations
	//

	/**
	 * Initializes a language manager with sensible defaults.
	 * 
	 * @param languageManager
	 *        The language manager
	 */
	public static void initializeLanguageManager( LanguageManager languageManager )
	{
		String javaScriptEngine = System.getProperty( "sincerity.javascript" );
		if( javaScriptEngine == null )
			javaScriptEngine = System.getenv( "SINCERITY_JAVASCRIPT" );
		if( javaScriptEngine == null )
			javaScriptEngine = "Nashorn";

		// Adapter preferences
		languageManager.getAttributes().put( LanguageManager.ADAPTER_PRIORITY_ATTRIBUTE + "Jython", 1 );
		languageManager.getAttributes().put( LanguageManager.ADAPTER_PRIORITY_ATTRIBUTE + javaScriptEngine, 1 );

		try
		{
			// Prefer log4j chute for Velocity if log4j exists
			Class.forName( "org.apache.log4j.Logger" );
			languageManager.getAttributes().putIfAbsent( "velocity.runtime.log.logsystem.class", "org.apache.velocity.runtime.log.Log4JLogChute" );
			languageManager.getAttributes().putIfAbsent( "velocity.runtime.log.logsystem.log4j.logger", "velocity" );
		}
		catch( ClassNotFoundException x )
		{
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private ScripturianUtil()
	{
	}
}

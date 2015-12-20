/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel;

import java.io.File;
import java.io.PrintWriter;

import com.threecrickets.creel.event.ConsoleEventHandler;
import com.threecrickets.creel.event.EventHandlers;
import com.threecrickets.creel.internal.Properties;

/**
 * @author Tal Liron
 */
public class Creel
{
	//
	// Main
	//

	public static void main( String[] arguments )
	{
		System.out.println( "Creel" );

		String propertiesPath = "creel.properties";
		String destinationPath = "zzz/jars";
		boolean quiet = false;
		boolean ansi = false;
		boolean overwrite = true;

		try
		{
			Properties properties = new Properties( new File( propertiesPath ) );

			Manager manager = new Manager();

			if( !quiet )
				( (EventHandlers) manager.getEventHandler() ).add( new ConsoleEventHandler( new PrintWriter( System.out, true ), ansi ) );

			manager.setExplicitModules( properties.getExplicitModuleConfigs() );
			manager.setRepositories( properties.getRepositoryConfigs() );

			manager.identify();
			manager.install( destinationPath, overwrite, true );
		}
		catch( Throwable x )
		{
			x.printStackTrace();
		}
	}
}

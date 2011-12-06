package com.threecrickets.sincerity.plugin.logging;

import java.io.File;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;

public class LoggingPlugin implements Plugin
{
	//
	// Plugin
	//

	public String getName()
	{
		return "logging";
	}

	public String[] getCommands()
	{
		return new String[]
		{
			"initialize", "log"
		};
	}

	public void run( String command, String[] arguments, Sincerity sincerity ) throws Exception
	{
		if( "initialize".equals( command ) )
		{
			initialize( sincerity );
		}
		else if( "log".equals( command ) )
		{
			initialize( sincerity );
			Logger.getLogger( "sincerity" ).info( arguments[0] );
		}
		else
			throw new Exception( "Unknown command: " + command );
	}

	//
	// Operations
	//

	public void initialize( Sincerity sincerity )
	{
		// Configure log4j
		try
		{
			File basePath = sincerity.getContainer().getRoot();
			System.setProperty( "sincerity.logs", new File( basePath, "logs" ).getAbsolutePath() );
			org.apache.log4j.PropertyConfigurator.configure( new File( basePath, "configuration/logging.conf" ).getAbsolutePath() );
		}
		catch( Exception x )
		{
		}

		// Remove any pre-existing configuration from JULI
		LogManager.getLogManager().reset();

		// Bridge JULI to SLF4J, which will in turn use log4j as its engine
		try
		{
			org.slf4j.bridge.SLF4JBridgeHandler.install();
		}
		catch( Exception x )
		{
		}
	}
}
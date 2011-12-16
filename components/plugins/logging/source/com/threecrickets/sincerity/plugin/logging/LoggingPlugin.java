package com.threecrickets.sincerity.plugin.logging;

import java.io.File;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.BadArgumentsCommandException;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownCommandException;

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

	public void run( Command command ) throws SincerityException
	{
		String name = command.getName();
		if( "initialize".equals( name ) )
		{
			initialize( command.getSincerity() );
		}
		else if( "log".equals( name ) )
		{
			String[] arguments = command.getArguments();
			if( arguments.length < 1 )
				throw new BadArgumentsCommandException( command, "message" );

			initialize( command.getSincerity() );
			Logger.getLogger( "sincerity" ).info( arguments[0] );
		}
		else
			throw new UnknownCommandException( command );
	}

	//
	// Operations
	//

	public void initialize( Sincerity sincerity )
	{
		// Configure log4j
		try
		{
			System.setProperty( "sincerity.logs", sincerity.getContainer().getFile( "logs" ).getPath() );
			File configurationFile = sincerity.getContainer().getFile( "configuration", "logging.conf" );
			if( configurationFile.exists() )
				org.apache.log4j.xml.DOMConfigurator.configureAndWatch( configurationFile.getPath() );
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
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
			"logging", "log"
		};
	}

	public void run( Command command ) throws SincerityException
	{
		String name = command.getName();
		if( "logging".equals( name ) )
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

	public void initialize( Sincerity sincerity ) throws SincerityException
	{
		// Configure log4j
		File configurationFile = sincerity.getContainer().getFile( "configuration", "logging.conf" );
		if( configurationFile.exists() )
		{
			// log4j configuration files can use this
			System.setProperty( "sincerity.logs", sincerity.getContainer().getFile( "logs" ).getPath() );

			try
			{
				sincerity.getContainer().getDependencies().updateClasspath();
				System.out.println(sincerity.getContainer().getDependencies().getClassLoader().loadClass( "org.apache.log4j.xml.DOMConfigurator" ));
				org.apache.log4j.xml.DOMConfigurator.configureAndWatch( configurationFile.getPath() );
			}
			catch( NoClassDefFoundError x )
			{
				x.printStackTrace();
				throw new SincerityException( "Could not find log4j in claspath", x );
			}
			catch( ClassNotFoundException e )
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Makes sure some servers (such as Jetty) don't log to console
		System.setProperty( "java.util.logging.config.file", "none" );

		// Remove any pre-existing configuration from JULI
		LogManager.getLogManager().reset();

		// Bridge JULI to SLF4J, which will in turn use log4j as its engine
		try
		{
			org.slf4j.bridge.SLF4JBridgeHandler.install();
		}
		catch( NoClassDefFoundError x )
		{
			throw new SincerityException( "Could not find SLF4J bridge in claspath", x );
		}
	}
}
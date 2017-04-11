/**
 * Copyright 2011-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.logging;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.util.NameUtil;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.util.CollectionUtil;

/**
 * A configuration that is prepared via API, rather than loading it from file.
 * 
 * @author Tal Liron
 */
public class ProgrammableConfiguration extends AbstractConfiguration
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param name
	 *        The configuration name
	 */
	public ProgrammableConfiguration( String name )
	{
		super( ConfigurationSource.NULL_SOURCE );
		setName( name );
	}

	//
	// Operations
	//

	/**
	 * Uses this configuration.
	 */
	public void use()
	{
		Sincerity sincerity = Sincerity.getCurrent();
		if( ( sincerity != null ) && sincerity.getVerbosity() >= 2 )
			sincerity.getOut().println( "Using Log4j configuration: " + getName() );

		new ProgrammableConfigurationFactory( this ).use();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected void doConfigure()
	{
		// Unfortunately, AbstractConfiguration uses a private "root" field,
		// which we cannot easily affect, and that field is used in its private
		// setParents() method. Thus, we will need to calculate the parenthood
		// ourselves, using our own known root.

		Sincerity sincerity = Sincerity.getCurrent();

		if( ( sincerity != null ) && sincerity.getVerbosity() >= 2 )
			sincerity.getOut().println( "Loggers:" );

		LoggerConfig root = getLogger( LogManager.ROOT_LOGGER_NAME );

		for( Map.Entry<String, LoggerConfig> entry : CollectionUtil.sortedMap( getLoggers() ).entrySet() )
		{
			String name = entry.getKey();
			LoggerConfig logger = entry.getValue();

			logger.setParent( null );
			if( !LogManager.ROOT_LOGGER_NAME.equals( name ) )
			{
				name = NameUtil.getSubName( name );
				while( name != null )
				{
					LoggerConfig parent = getLogger( name );
					if( parent != null )
					{
						logger.setParent( parent );
						break;
					}
					name = NameUtil.getSubName( name );
				}

				if( logger.getParent() == null )
					logger.setParent( root );
			}

			if( ( sincerity != null ) && sincerity.getVerbosity() >= 2 )
			{
				sincerity.getOut()
					.println( "  \"" + logger.getName() + "\" (" + logger.getLevel() + ( logger.isAdditive() ? ") +" : ") " ) + ( logger.getParent() == null ? "" : "> \"" + logger.getParent().getName() + "\"" ) );

				for( Appender appender : logger.getAppenders().values() )
					sincerity.getOut().println( "    -> \"" + appender.getName() + "\"" );
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

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

import java.net.URI;

import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

/**
 * A simple configuration factory that returns a specified instance of
 * {@link ProgrammableLog4jConfiguration}.
 * 
 * @author Tal Liron
 */
public class ProgrammableLog4jConfigurationFactory extends ConfigurationFactory
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param configuration
	 *        The configuration
	 */
	public ProgrammableLog4jConfigurationFactory( Configuration configuration )
	{
		super();
		this.configuration = configuration;
	}

	//
	// Operations
	//

	/**
	 * Uses this configuration factory.
	 */
	public void use()
	{
		setConfigurationFactory( this );
	}

	//
	// ConfigurationFactory
	//

	public Configuration getConfiguration( ConfigurationSource source )
	{
		return configuration;
	}

	@Override
	public Configuration getConfiguration( final String name, final URI configLocation )
	{
		return configuration;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected String[] getSupportedTypes()
	{
		return SUPPORTED_TYPES;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final static String[] SUPPORTED_TYPES = new String[0];

	private final Configuration configuration;
}

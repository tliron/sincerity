package com.threecrickets.sincerity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.threecrickets.sincerity.exception.NoContainerException;

public class Command
{
	//
	// Construction
	//

	public Command( String name, Sincerity sincerity )
	{
		String[] split = name.split( ":", 2 );
		if( split.length == 2 )
		{
			plugin = split[0];
			this.name = split[1];
		}
		else
			this.name = name;

		this.sincerity = sincerity;
	}

	//
	// Attributes
	//

	public String getName()
	{
		return name;
	}

	public Sincerity getSincerity()
	{
		return sincerity;
	}

	public String[] getArguments() throws Exception
	{
		if( arguments == null && parse )
			parse();
		else
			arguments = rawArguments.toArray( new String[rawArguments.size()] );
		return arguments;
	}

	public List<String> getSwitches() throws Exception
	{
		if( switches == null && parse )
			parse();
		else
			switches = Collections.emptyList();
		return switches;
	}

	public Map<String, String> getProperties() throws Exception
	{
		if( properties == null && parse )
			parse();
		else
			properties = Collections.emptyMap();
		return properties;
	}

	public boolean getParse()
	{
		return parse;
	}

	public void setParse( boolean parse )
	{
		this.parse = parse;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		if( plugin == null )
			return name;
		return plugin + ":" + name;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected String plugin;

	protected final List<String> rawArguments = new ArrayList<String>();

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String name;

	private final Sincerity sincerity;

	private boolean parse;

	private String[] arguments;

	private List<String> switches;

	private Map<String, String> properties;

	private void parse() throws Exception
	{
		ArrayList<String> arguments = new ArrayList<String>();
		switches = new ArrayList<String>();
		properties = new HashMap<String, String>();

		for( String argument : rawArguments )
		{
			if( argument.startsWith( "--" ) )
			{
				argument = argument.substring( 2 );
				if( argument.length() > 0 )
					switches.add( argument );
			}
			else
			{
				try
				{
					sincerity.getContainer().getAliases().addArgument( argument, arguments );
				}
				catch( NoContainerException x )
				{
					arguments.add( argument );
				}
			}
		}

		for( String theSwitch : switches )
		{
			String[] split = theSwitch.split( "=", 2 );
			if( split.length == 2 )
				properties.put( split[0], split[1] );
		}

		this.arguments = arguments.toArray( new String[arguments.size()] );
	}
}

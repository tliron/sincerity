package com.threecrickets.sincerity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Command
{
	//
	// Construction
	//

	public Command( String name, boolean parse )
	{
		String[] split = name.split( ":", 2 );
		if( split.length == 2 )
		{
			plugin = split[0];
			this.name = split[1];
		}
		else
			this.name = name;

		this.parse = parse;
	}

	//
	// Attributes
	//

	public final String name;

	public String plugin;

	public final List<String> rawArguments = new ArrayList<String>();

	public String[] getArguments()
	{
		if( arguments == null && parse )
			parse();
		else
			arguments = rawArguments.toArray( new String[rawArguments.size()] );
		return arguments;
	}

	public List<String> getSwitches()
	{
		if( switches == null && parse )
			parse();
		else
			switches = Collections.emptyList();
		return switches;
	}

	public Map<String, String> getProperties()
	{
		if( properties == null && parse )
			parse();
		else
			properties = Collections.emptyMap();
		return properties;
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
	// Private

	private final boolean parse;

	private String[] arguments;

	private List<String> switches;

	private Map<String, String> properties;

	private void parse()
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
				arguments.add( argument );
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

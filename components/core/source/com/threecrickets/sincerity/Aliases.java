package com.threecrickets.sincerity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class Aliases implements Iterable<String>
{
	//
	// Construction
	//

	public Aliases( File file )
	{
		this.file = file;
	}

	//
	// Attributes
	//

	public String[] get( String alias )
	{
		validate();

		Object value = properties.get( alias );
		if( value != null )
		{
			ArrayList<String> arguments = new ArrayList<String>();
			for( String argument : value.toString().split( " " ) )
				addArgument( argument, arguments );
			return arguments.toArray( new String[arguments.size()] );
		}

		return null;
	}

	public void addArgument( String argument, List<String> arguments )
	{
		if( argument.startsWith( "@" ) )
		{
			String[] alias = get( argument.substring( 1 ) );
			if( alias != null )
			{
				for( String a : alias )
					addArgument( a, arguments );
			}
		}
		else
			arguments.add( argument );
	}

	//
	// Iterable
	//

	public Iterator<String> iterator()
	{
		validate();

		ArrayList<String> aliases = new ArrayList<String>();
		for( Object alias : properties.keySet() )
			aliases.add( alias.toString() );

		return aliases.iterator();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private Properties properties;

	private void validate()
	{
		if( properties == null )
		{
			properties = new Properties();
			try
			{
				FileInputStream stream = new FileInputStream( file );
				try
				{
					properties.load( stream );
				}
				finally
				{
					stream.close();
				}
			}
			catch( FileNotFoundException x )
			{
			}
			catch( IOException x )
			{
			}
		}
	}
}

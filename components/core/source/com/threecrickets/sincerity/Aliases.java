package com.threecrickets.sincerity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
			return value.toString().split( " " );
		return null;
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

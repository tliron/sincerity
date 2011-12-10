package com.threecrickets.sincerity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class Aliases
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

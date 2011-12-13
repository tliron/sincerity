package com.threecrickets.sincerity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.exception.UnknownShortcutException;

public class Shortcuts implements Iterable<String>
{
	//
	// Construction
	//

	public Shortcuts( File file ) throws SincerityException
	{
		this.file = file;
		validate();
	}

	//
	// Attributes
	//

	public String[] get( String shortcut ) throws SincerityException
	{
		Object value = properties.get( shortcut );

		if( value == null )
			throw new UnknownShortcutException( shortcut );

		ArrayList<String> items = new ArrayList<String>();
		for( String argument : value.toString().split( " " ) )
			addArgument( argument, items );
		return items.toArray( new String[items.size()] );
	}

	public void addArgument( String argument, List<String> arguments ) throws SincerityException
	{
		if( argument.startsWith( "@" ) )
		{
			String shortcut = argument.substring( 1 );
			String[] items = get( shortcut );
			if( items == null )
				throw new UnknownShortcutException( shortcut );

			for( String a : items )
				addArgument( a, arguments );
		}
		else
			arguments.add( argument );
	}

	//
	// Iterable
	//

	public Iterator<String> iterator()
	{
		ArrayList<String> shortcuts = new ArrayList<String>();
		for( Object shortcut : properties.keySet() )
			shortcuts.add( shortcut.toString() );

		return shortcuts.iterator();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File file;

	private Properties properties;

	private void validate() throws SincerityException
	{
		if( properties == null )
		{
			properties = new Properties();
			try
			{
				FileInputStream stream = new FileInputStream( file );
				try
				{
					try
					{
						properties.load( stream );
					}
					finally
					{
						stream.close();
					}
				}
				catch( IOException x )
				{
					throw new SincerityException( "Could not read shortcuts configuration: " + file, x );
				}
			}
			catch( FileNotFoundException x )
			{
			}
		}
	}
}

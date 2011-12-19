package com.threecrickets.sincerity.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

public class Pipe implements Runnable
{
	//
	// Construction
	//

	public Pipe( Reader reader, Writer writer )
	{
		this.reader = reader;
		this.writer = writer;
	}

	//
	// Runnable
	//

	public void run()
	{
		BufferedReader bufferedReader = new BufferedReader( reader );
		PrintWriter printWriter = writer instanceof PrintWriter ? (PrintWriter) writer : new PrintWriter( writer, true );
		try
		{
			String line;
			while( ( line = bufferedReader.readLine() ) != null )
			{
				printWriter.println( line );
				printWriter.flush();
			}
		}
		catch( IOException x )
		{
		}
		finally
		{
			try
			{
				bufferedReader.close();
			}
			catch( IOException x )
			{
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Reader reader;

	private final Writer writer;
}

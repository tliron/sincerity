package com.threecrickets.sincerity.internal;

import java.util.Iterator;

public class StringUtil
{
	public static String join( Iterable<String> strings, String delimiter )
	{
		StringBuilder r = new StringBuilder();
		for( Iterator<String> i = strings.iterator(); i.hasNext(); )
		{
			r.append( i.next() );
			if( i.hasNext() )
				r.append( delimiter );
		}
		return r.toString();
	}
}

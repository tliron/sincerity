/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.maven.internal;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import com.threecrickets.creel.util.XmlUtil;

/**
 * @author Tal Liron
 */
public class Properties extends HashMap<String, String>
{
	//
	// Construction
	//

	public Properties( Element element )
	{
		if( element != null )
			for( Element property : new XmlUtil.Elements( element ) )
			{
				String name = property.getTagName();
				String value = property.getTextContent();
				put( name, value );
			}
	}

	//
	// Operations
	//

	public String interpolate( String string )
	{
		if( string == null )
			return null;
		Matcher matcher = INTERPOLATION_PATTERN.matcher( string );
		if( !matcher.find() )
			return string;
		StringBuffer r = new StringBuffer();
		do
		{
			String key = matcher.group( 1 );
			String value = get( key );
			matcher.appendReplacement( r, Matcher.quoteReplacement( value != null ? value : matcher.group() ) );
		}
		while( matcher.find() );
		matcher.appendTail( r );
		return r.toString();
	}

	public String interpolate( String string, String key )
	{
		string = interpolate( string );
		if( string != null )
			put( key, string );
		return string;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * \$\{([\w\.]+)\}
	 */
	private final static Pattern INTERPOLATION_PATTERN = Pattern.compile( "\\$\\{([\\w\\.]+)\\}" );

	private static final long serialVersionUID = 1L;
}

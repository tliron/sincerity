package com.threecrickets.sincerity.util;

import java.util.regex.Pattern;

/**
 * Glob utilities.
 * 
 * @author Tal Liron
 */
public abstract class GlobUtil
{
	/**
	 * Tests if a string matches a glob pattern.
	 * <p>
	 * The pattern may contain any number of '*' or '?' wildcards. Escape '*' or
	 * '?' using a preceding '\'.
	 * 
	 * @param string
	 *        The string to test
	 * @param pattern
	 *        The pattern
	 * @return True if the string matches the pattern
	 */
	public static boolean matches( String string, String pattern )
	{
		if( ( pattern == null ) || pattern.isEmpty() || pattern.equals( "*" ) )
			// Match everything
			return true;

		String regex = toRegEx( pattern );
		return Pattern.matches( regex, string );
	}

	/**
	 * Transforms a glob pattern into a compiled regular expression.
	 * <p>
	 * The pattern may contain any number of '*' or '?' wildcards. Escape '*' or
	 * '?' using a preceding '\'.
	 * 
	 * @param pattern
	 *        The pattern
	 * @return The compiled regular expression.
	 */
	public static Pattern toPattern( String pattern )
	{
		return Pattern.compile( toRegEx( pattern ) );
	}

	/**
	 * Transforms a glob pattern into a regular expression.
	 * <p>
	 * The pattern may contain any number of '*' or '?' wildcards. Escape '*' or
	 * '?' using a preceding '\'.
	 * 
	 * @param pattern
	 *        The pattern
	 * @return The regular expression
	 */
	public static String toRegEx( String pattern )
	{
		StringBuilder regex = new StringBuilder();

		int length = pattern.length();
		char lastC = 0;
		int lastI = 0;
		for( int i = 0; i < length; i++ )
		{
			char c = pattern.charAt( i );

			if( ( ( c == '*' ) || ( c == '?' ) ) && ( lastC != '\\' ) )
			{
				regex.append( Pattern.quote( pattern.substring( lastI, i ) ) );
				regex.append( "[\\s\\S]" );
				if( c == '*' )
					regex.append( '*' );
				lastI = i + 1;
			}

			lastC = c;
		}

		if( lastI < length )
			regex.append( Pattern.quote( pattern.substring( lastI ) ) );

		regex.insert( 0, '^' );
		regex.append( '$' );

		return regex.toString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private GlobUtil()
	{
	}
}

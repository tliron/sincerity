package com.threecrickets.creel.maven.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Versions should have forms such as '1.0' or '2.4.1-beta1'.
 * <p>
 * When comparing them, we first take into account the dot-separated integer
 * parts. In case both versions are identical on those terms, then the postfix
 * after the dash is compared.
 * <p>
 * Postfix comparison takes into account its semantic meaning. Thus, 'beta2'
 * would be greater than 'alpha3', and 'alpha3' would be greater than 'dev12'.
 * 
 * @author Tal Liron
 */
public class Version implements Comparable<Version>
{
	//
	// Constants
	//

	public static final Map<String, Double> POSTFIXES;

	static
	{
		HashMap<String, Double> postfixes = new HashMap<String, Double>();
		postfixes.put( "d", -3.0 );
		postfixes.put( "dev", -3.0 );
		postfixes.put( "a", -2.0 );
		postfixes.put( "alpha", -2.0 );
		postfixes.put( "b", -1.0 );
		postfixes.put( "beta", -1.0 );
		POSTFIXES = Collections.unmodifiableMap( postfixes );
	}

	//
	// Construction
	//

	public Version( String version )
	{
		version = version == null ? "" : version.trim();
		text = version;

		// TODO: regexp match to see if it's parseable

		// Main and postfix separated by a dash
		int dash = version.indexOf( '-' );
		String main = dash == -1 ? version : version.substring( 0, dash );
		String postfix = dash == -1 ? null : version.substring( dash + 1 );

		// The main parts are separated by dots
		String[] parts = main.split( "." );
		int partsLength = parts.length;
		this.parts = new int[partsLength];
		for( int i = 0; i < partsLength; i++ )
			this.parts[i] = Integer.parseInt( parts[i] );

		if( postfix != null )
		{
			// The postfix is separated into text and then an integer
			int postfixFirstDigit = 0;
			int postfixLength = postfix.length();
			while( ( postfixFirstDigit < postfixLength ) && !Character.isDigit( postfix.charAt( postfixFirstDigit ) ) )
				postfixFirstDigit++;
			String postfixText = postfixFirstDigit == postfixLength ? postfix : postfix.substring( 0, postfixFirstDigit );
			int postfixInteger = postfixFirstDigit == postfixLength ? 0 : Integer.parseInt( postfix.substring( postfixFirstDigit ) );

			// Convert postfix text and number into the extra value
			Double postfixValue = POSTFIXES.get( postfixText.toLowerCase() );
			double extra = postfixValue != null ? postfixValue : 0.0;
			this.extra = extra + ( postfixInteger / 10.0 );
		}
		else
			extra = 0.0;
	}

	//
	// Comparable
	//

	public int compareTo( Version version )
	{
		if( version == null )
			throw new NullPointerException();

		// Non-parseable versions will revert to a lexigraphic comparison
		if( ( parts.length == 0 ) || ( version.parts.length == 0 ) )
			return text.compareTo( version.text );

		int length1 = parts.length;
		int length2 = version.parts.length;
		int length = Math.max( length1, length2 );
		for( int p = 0; p < length; p++ )
		{
			Integer part1 = p <= length1 - 1 ? parts[p] : null;
			Integer part2 = p <= length2 - 1 ? version.parts[p] : null;
			if( ( part1 == null ) && ( part2 == null ) )
				return 0;
			if( part1 == null )
				return -1;
			if( part2 == null )
				return 1;
			if( part1 != part2 )
				return part1 - part2 > 0 ? 1 : -1;
			// Equal, so continue
		}

		if( extra != version.extra )
			return version.extra - version.extra > 0.0 ? 1 : -1;

		return 0;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return text;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String text;

	private final int[] parts;

	private final double extra;
}
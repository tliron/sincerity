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
		postfixes.put( "dev", -4.0 );
		postfixes.put( "d", -4.0 );
		postfixes.put( "milestone", -3.0 );
		postfixes.put( "m", -3.0 );
		postfixes.put( "alpha", -2.0 );
		postfixes.put( "a", -2.0 );
		postfixes.put( "beta", -1.0 );
		postfixes.put( "b", -1.0 );
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

		// Main and postfix are usually separated by a dash
		String main, postfix;
		int dash = version.indexOf( '-' );
		if( dash != -1 )
		{
			main = version.substring( 0, dash );
			postfix = version.substring( dash + 1 );
		}
		else
		{
			// ...but sometimes the postfix just starts with a letter
			int letter = -1;
			for( int i = 0, length = version.length(); i < length; i++ )
			{
				if( Character.isLetter( version.charAt( i ) ) )
				{
					letter = i;
					break;
				}
			}
			if( letter != -1 )
			{
				main = version.substring( 0, letter );
				postfix = version.substring( letter );
			}
			else
			{
				main = version;
				postfix = null;
			}
		}

		// The main parts are separated by dots
		String[] parts = main.split( "\\." );
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

		int length1 = parts.length;
		int length2 = version.parts.length;

		// Non-parseable versions will revert to a lexigraphic comparison
		if( ( length1 == 0 ) || ( length2 == 0 ) )
			return text.compareTo( version.text );

		int length = length1 > length2 ? length1 : length2;
		for( int p = 0; p < length; p++ )
		{
			int part1 = ( p < length1 ) ? parts[p] : 0;
			int part2 = ( p < length2 ) ? version.parts[p] : 0;
			if( part1 == part2 )
				continue;
			return part2 > part1 ? -1 : 1;
		}

		if( extra != version.extra )
			return version.extra > extra ? -1 : 1;

		return 0;
	}

	public int compareToPrint( Version version )
	{
		int compare = compareTo( version );
		char sign;
		if( compare < 0 )
			sign = '<';
		else if( compare > 0 )
			sign = '>';
		else
			sign = '=';
		System.out.println( this + " " + sign + " " + version );
		return compare;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		int length = parts.length;
		if( length > 0 )
		{
			StringBuilder r = new StringBuilder();
			for( int i = 0; i < length; i++ )
			{
				r.append( parts[i] );
				if( i < length - 1 )
					r.append( '.' );
			}
			if( extra != 0.0 )
			{
				r.append( '+' );
				r.append( extra );
			}
			return r.toString();
		}
		return text;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String text;

	private final int[] parts;

	private final double extra;
}
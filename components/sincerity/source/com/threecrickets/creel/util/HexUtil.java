/**
 * /** Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.util;

/**
 * Hex utilities.
 * 
 * @author Tal Liron
 */
public abstract class HexUtil
{
	//
	// Static operations
	//

	/**
	 * Creates a hexadecimal representation for an array of bytes.
	 * <p>
	 * The letters A-F are in uppercase.
	 * 
	 * @param bytes
	 *        The bytes
	 * @return The hexadecimal representation
	 */
	public static String toHex( byte[] bytes )
	{
		// BigInteger i = new BigInteger( 1, bytes );
		// return String.format( "%0" + ( bytes.length << 1 ) + "x", i );

		// See: http://stackoverflow.com/a/9855338/849021

		char[] hexChars = new char[bytes.length * 2];
		int v;
		for( int j = 0; j < bytes.length; j++ )
		{
			v = bytes[j] & 0xFF;
			hexChars[j * 2] = HEX[v >>> 4];
			hexChars[j * 2 + 1] = HEX[v & 0x0F];
		}
		return new String( hexChars );
	}

	/**
	 * Creates an array of bytes from its hexadecimal representation.
	 * 
	 * @param hex
	 *        The hexadecimal representation
	 * @return The bytes
	 */
	public static byte[] fromHex( String hex )
	{
		int length = hex.length();
		byte[] data = new byte[length / 2];
		for( int i = 0; i < length; i += 2 )
			data[i / 2] = (byte) ( ( Character.digit( hex.charAt( i ), 16 ) << 4 ) + Character.digit( hex.charAt( i + 1 ), 16 ) );
		return data;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private HexUtil()
	{
	}

	private static final char[] HEX =
	{
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	};
}

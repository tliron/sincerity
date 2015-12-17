package com.threecrickets.creel.maven.internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.threecrickets.sincerity.util.IoUtil;
import com.threecrickets.sincerity.util.StringUtil;

public class Signature
{
	//
	// Construction
	//

	public Signature( URL url, boolean allowMd5 ) throws IOException
	{
		// Try SHA-1 first
		String algorithm, content;
		URL signatureUrl = new URL( url.toString() + ".sha1" );
		try
		{
			content = IoUtil.readText( signatureUrl );
			content = content.substring( 0, 40 ).toUpperCase();
			if( content.length() != 40 )
				throw new RuntimeException( "SHA-1 signatures must have 40 characters" );
			algorithm = "SHA-1";
		}
		catch( IOException x )
		{
			if( allowMd5 )
			{
				// Fallback to MD5
				signatureUrl = new URL( url.toString() + ".md5" );
				content = IoUtil.readText( signatureUrl );
				content = content.substring( 0, 32 );
				if( content.length() != 32 )
					throw new RuntimeException( "MD5 signatures must have 32 characters" );
				algorithm = "MD5";
			}
			else
				throw x;
		}

		this.algorithm = algorithm;
		digestHex = content.toUpperCase();
	}

	//
	// Operations
	//

	public boolean validate( File file ) throws IOException
	{
		return validate( file.toURI().toURL() );
	}

	public boolean validate( URL url ) throws IOException
	{
		byte[] digest = IoUtil.getDigest( url, algorithm );
		String digestHex = StringUtil.toHex( digest );
		return this.digestHex.equals( digestHex );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String algorithm;

	private final String digestHex;
}

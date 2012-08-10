/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.internal;

import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.sincerity.Sincerity;

/**
 * Puts the "sincerity" service in the execution context.
 * 
 * @author Tal Liron
 */
public class SincerityExecutionController implements ExecutionController
{
	//
	// Construction
	//

	public SincerityExecutionController( Sincerity sincerity )
	{
		this.sincerity = sincerity;
	}

	//
	// ExecutionController
	//

	public void initialize( ExecutionContext executionContext ) throws ExecutionException
	{
		executionContext.getServices().put( "sincerity", sincerity );
	}

	public void release( ExecutionContext executionContext )
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Sincerity sincerity;
}

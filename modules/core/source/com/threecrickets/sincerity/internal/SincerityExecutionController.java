package com.threecrickets.sincerity.internal;

import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.sincerity.Sincerity;

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

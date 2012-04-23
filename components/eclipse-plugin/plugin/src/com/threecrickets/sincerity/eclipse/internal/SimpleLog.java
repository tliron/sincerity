package com.threecrickets.sincerity.eclipse.internal;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;

public class SimpleLog
{
	//
	// Construction
	//

	public SimpleLog( ILog log, String id )
	{
		this.log = log;
		this.id = id;
	}

	public SimpleLog( String id )
	{
		this( Platform.getLog( Platform.getBundle( id ) ), id );
	}

	//
	// Operations
	//

	public void log( int severity, Throwable x )
	{
		log.log( new Status( severity, id, IStatus.OK, x.getMessage(), x ) );
	}

	public void log( int severity, String message )
	{
		log.log( new Status( severity, id, IStatus.OK, message, null ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final ILog log;

	private final String id;
}

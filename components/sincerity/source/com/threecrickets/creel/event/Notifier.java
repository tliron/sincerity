package com.threecrickets.creel.event;

import java.util.UUID;

import com.threecrickets.creel.event.Event.Type;

public class Notifier
{
	//
	// Construction
	//

	public Notifier()
	{
		this( new NullEventHandler() );
	}

	public Notifier( EventHandler eventHandler )
	{
		this.eventHandler = eventHandler;
	}

	//
	// Attributes
	//

	public EventHandler getEventHandler()
	{
		return eventHandler;
	}

	public void setEventHandler( EventHandler eventHandler )
	{
		this.eventHandler = eventHandler;
	}

	//
	// Operations
	//

	public String newId()
	{
		return UUID.randomUUID().toString();
	}

	public void fireEvent( Type type, String id, CharSequence message, Double progress, Throwable exception )
	{
		eventHandler.handleEvent( new Event( type, id, message, progress, exception ) );
	}

	public void info( CharSequence message )
	{
		fireEvent( Event.Type.INFO, null, message, null, null );
	}

	public void debug( CharSequence message )
	{
		// TODO
	}

	public String begin( CharSequence message )
	{
		String id = newId();
		fireEvent( Event.Type.BEGIN, id, message, null, null );
		return id;
	}

	public String begin( CharSequence message, double progress )
	{
		String id = newId();
		fireEvent( Event.Type.BEGIN, id, message, progress, null );
		return id;
	}

	public void update( String id, CharSequence message, double progress )
	{
		fireEvent( Event.Type.UPDATE, id, message, progress, null );
	}

	public void update( String id, CharSequence message )
	{
		fireEvent( Event.Type.UPDATE, id, message, null, null );
	}

	public void update( String id, double progress )
	{
		fireEvent( Event.Type.UPDATE, id, null, progress, null );
	}

	public void end( String id, CharSequence message )
	{
		fireEvent( Event.Type.END, id, message, null, null );
	}

	public void fail( String id, CharSequence message )
	{
		fireEvent( Event.Type.FAIL, id, message, null, null );
	}

	public void error( CharSequence message )
	{
		fireEvent( Event.Type.ERROR, null, message, null, null );
	}

	public void error( Throwable exception )
	{
		fireEvent( Event.Type.ERROR, null, exception.getMessage(), null, exception );
	}

	public void error( CharSequence message, Throwable exception )
	{
		fireEvent( Event.Type.ERROR, null, message, null, exception );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private volatile EventHandler eventHandler;
}

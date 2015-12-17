package com.threecrickets.creel.event;

public class Event
{
	public enum Type
	{
		INFO, BEGIN, END, FAIL, UPDATE, ERROR
	}

	//
	// Construction
	//

	public Event( Type type, String id, String message, Double progress, Throwable exception )
	{
		this.type = type;
		this.id = id;
		this.message = message;
		this.progress = progress;
		this.exception = exception;
	}

	//
	// Attributes
	//

	public Type getType()
	{
		return type;
	}

	public String getId()
	{
		return id;
	}

	public String getMessage()
	{
		return message;
	}

	public Double getProgress()
	{
		return progress;
	}

	public Throwable getException()
	{
		return exception;
	}

	public void update( Event event )
	{
		if( event.message != null )
			message = event.message;
		if( event.progress != null )
			progress = event.progress;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Type type;

	private final String id;

	private String message;

	private Double progress;

	private final Throwable exception;
}

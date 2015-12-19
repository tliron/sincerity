/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.event;

/**
 * @author Tal Liron
 */
public class Event
{
	public enum Type
	{
		INFO, BEGIN, END, FAIL, UPDATE, ERROR
	}

	//
	// Construction
	//

	public Event( Type type, String id, CharSequence message, Double progress, Throwable exception )
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

	public CharSequence getMessage()
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

	private CharSequence message;

	private Double progress;

	private final Throwable exception;
}

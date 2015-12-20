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

import java.io.PrintWriter;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tal Liron
 */
public class ConsoleEventHandler implements EventHandler
{
	//
	// Construction
	//

	public ConsoleEventHandler( PrintWriter out, boolean ansi )
	{
		this.out = out;
		this.ansi = ansi;
	}

	//
	// EventHandler
	//

	public synchronized boolean handleEvent( Event event )
	{
		// Move up before the ongoing block we printed last time
		int ongoingEventsHeight = ongoingEvents.size();
		if( ongoingEventsHeight > 0 )
			controlSequence( "" + ongoingEventsHeight + 'A' );

		Event.Type type = event.getType();
		if( type == Event.Type.BEGIN )
		{
			// Add ongoing event
			if( event.getId() != null )
				ongoingEvents.add( event );
		}
		else if( ( type == Event.Type.END ) || ( type == Event.Type.FAIL ) )
		{
			// Remove ongoing event
			String id = event.getId();
			for( Event ongoingEvent : ongoingEvents )
			{
				if( ongoingEvent.getId().equals( id ) )
				{
					ongoingEvents.remove( ongoingEvent );
					break;
				}
			}
			// This line will take the place of the line we removed
			controlSequence( ( type == Event.Type.FAIL ? failGraphics : endGraphics ) + 'm' );
			print( event );
		}
		else if( type == Event.Type.UPDATE )
		{
			// Update ongoing event
			String id = event.getId();
			for( Event ongoingEvent : ongoingEvents )
			{
				if( ongoingEvent.getId().equals( id ) )
				{
					ongoingEvent.update( event );
					break;
				}
			}
		}
		else if( type == Event.Type.ERROR )
		{
			controlSequence( errorGraphics + 'm' );
			print( event );
		}
		else
		{
			controlSequence( defaultGraphics + 'm' );
			print( event );
		}

		// Print ongoing block after everything else
		if( ansi )
			for( Event ongoingEvent : ongoingEvents )
			{
				controlSequence( ongoingGraphics + 'm' );
				print( ongoingEvent );
			}

		// Erase to end of screen
		controlSequence( "0J" );

		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected int getTerminalWidth()
	{
		return Integer.MAX_VALUE;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * ANSI CSI (Control Sequence Introducer).
	 */
	private static final String CSI = "\033[";

	private final PrintWriter out;

	private final boolean ansi;

	private Collection<Event> ongoingEvents = new CopyOnWriteArrayList<Event>();

	private volatile String endGraphics = "32";

	private volatile String failGraphics = "31";

	private volatile String errorGraphics = "31";

	private volatile String ongoingGraphics = "33";

	private volatile String defaultGraphics = "34";

	private volatile int progressLength = 16;

	private volatile String progressStart = "[";

	private volatile String progressEnd = "] ";

	private volatile String progressDone = "=";

	private volatile String progressTodo = " ";

	private void print( Event event )
	{
		StringBuilder output = new StringBuilder();

		Double progress = event.getProgress();
		if( progress != null )
		{
			output.append( progressStart );
			for( int i = 0; i < progressLength; i++ )
				output.append( ( ( progress * progressLength ) > i ) ? progressDone : progressTodo );
			// TODO: spinner at end
			output.append( progressEnd );
		}

		CharSequence message = event.getMessage();
		if( message != null )
			output.append( message );

		// We are making sure that we always advance one row only, even if we
		// print a line longer than a row
		int length = output.length();
		int terminalWidth = getTerminalWidth();
		if( length >= terminalWidth )
		{
			// Will automatically advance to the next line
			out.print( output.substring( 0, terminalWidth ) );
		}
		else
		{
			out.print( output );
			// Reset graphics and erase to end of line
			controlSequence( "0m", "K" );
			out.println();
		}

		// Exception stack trace
		Throwable exception = event.getException();
		if( exception != null )
		{
			controlSequence( this.errorGraphics + 'm' );
			exception.printStackTrace( out );
			controlSequence( "0m" );
		}
	}

	private void controlSequence( CharSequence... args )
	{
		if( !ansi )
			return;
		for( CharSequence c : args )
		{
			out.print( CSI );
			out.print( c );
		}
	}
}

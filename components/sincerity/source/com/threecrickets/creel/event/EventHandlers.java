package com.threecrickets.creel.event;

import java.util.concurrent.CopyOnWriteArrayList;

public class EventHandlers extends CopyOnWriteArrayList<EventHandler> implements EventHandler
{
	//
	// EventHandler
	//

	public boolean handleEvent( Event event )
	{
		for( EventHandler eventHandler : this )
		{
			if( eventHandler.handleEvent( event ) )
				return true;
		}
		return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

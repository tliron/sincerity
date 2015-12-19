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

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tal Liron
 */
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

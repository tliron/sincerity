package com.threecrickets.creel.event;

public interface EventHandler
{
	/**
	 * @param event
	 * @return True if the event was swallowed
	 */
	public boolean handleEvent( Event event );
}

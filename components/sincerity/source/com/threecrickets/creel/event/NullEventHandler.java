package com.threecrickets.creel.event;

public class NullEventHandler implements EventHandler
{
	//
	// EventHandler
	//

	public boolean handleEvent( Event event )
	{
		return false;
	}
}

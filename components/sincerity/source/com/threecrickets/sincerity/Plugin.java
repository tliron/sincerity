package com.threecrickets.sincerity;

import com.threecrickets.sincerity.exception.SincerityException;

public interface Plugin
{
	public String getName();

	public String[] getCommands() throws SincerityException;

	public void run( Command command ) throws SincerityException;
}

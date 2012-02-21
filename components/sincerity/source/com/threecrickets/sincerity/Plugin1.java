package com.threecrickets.sincerity;

import com.threecrickets.sincerity.exception.SincerityException;

public interface Plugin1
{
	public int getVersion();

	public String getName();

	public String[] getCommands() throws SincerityException;

	public void run( Command command ) throws SincerityException;

	public void gui( Command command ) throws SincerityException;
}

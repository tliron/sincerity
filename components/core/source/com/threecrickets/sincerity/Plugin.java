package com.threecrickets.sincerity;

public interface Plugin
{
	public String getName();

	public String[] getCommands();

	public void run( Command command, Sincerity sincerity ) throws Exception;
}

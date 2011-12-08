package com.threecrickets.sincerity;

public interface Plugin
{
	public String getName();

	public String[] getCommands();

	public void run( String command, String[] arguments, Sincerity sincerity ) throws Exception;
}

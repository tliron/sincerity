/**
 * Copyright 2011-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

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

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

package com.threecrickets.sincerity.plugin.gui.internal;

import java.awt.Component;
import java.util.ArrayList;

import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.threecrickets.sincerity.plugin.gui.Refreshable;

/**
 */
public class LazyTabbedPane extends JTabbedPane implements ChangeListener
{
	//
	// Construction
	//

	public LazyTabbedPane()
	{
		super();
		addChangeListener( this );
	}

	//
	// ChangeListener
	//

	public void stateChanged( ChangeEvent event )
	{
		Component component = getSelectedComponent();
		if( component instanceof Refreshable )
		{
			Refreshable refreshable = (Refreshable) component;
			if( !( initialized.contains( refreshable ) ) )
			{
				initialized.add( refreshable );
				refreshable.refresh();
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	private final ArrayList<Refreshable> initialized = new ArrayList<Refreshable>();
}
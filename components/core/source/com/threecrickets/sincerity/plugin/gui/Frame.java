package com.threecrickets.sincerity.plugin.gui;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.exception.SincerityException;

public class Frame extends JFrame
{
	public Frame( Sincerity sincerity ) throws SincerityException
	{
		super( "Sincerity" );

		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

		JTabbedPane panes = new JTabbedPane();
		getContentPane().add( panes );

		panes.add( "Dependencies", new DependenciesPane( sincerity.getContainer().getDependencies() ) );
		panes.add( "Artifacts", new ArtifactsPane( sincerity.getContainer().getDependencies() ) );
		panes.add( "Licenses", new LicensesPane( sincerity.getContainer().getDependencies() ) );

		Rectangle bounds = getGraphicsConfiguration().getBounds();
		int width = bounds.width / 2;
		int height = bounds.height / 2;
		int centerX = bounds.x + bounds.width / 2;
		int centerY = bounds.y + bounds.height / 2;
		setLocation( centerX - width / 2, centerY - height / 2 );
		setPreferredSize( new Dimension( width, height ) );
		pack();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}

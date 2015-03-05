/**
 * Copyright 2011-2015 Three Crickets LLC.
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
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * A Swing list that supports a model of {@link JCheckBox} instances.
 * <p>
 * <a href="http://www.devx.com/tips/Tip/5342">Source</a>.
 */
public class CheckBoxList extends JList<JCheckBox>
{
	//
	// Construction
	//

	public CheckBoxList( JCheckBox[] listData )
	{
		super( listData );
		setCellRenderer( new CellRenderer() );
		addMouseListener( new MouseAdapter()
		{
			@Override
			public void mousePressed( MouseEvent e )
			{
				int index = locationToIndex( e.getPoint() );
				if( index != -1 )
				{
					JCheckBox checkbox = getModel().getElementAt( index );
					checkbox.setSelected( !checkbox.isSelected() );
					repaint();
				}
			}
		} );

		setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected class CellRenderer implements ListCellRenderer<JCheckBox>
	{
		public Component getListCellRendererComponent( JList<? extends JCheckBox> list, JCheckBox checkbox, int index, boolean isSelected, boolean cellHasFocus )
		{
			checkbox.setFont( getFont() );
			checkbox.setFocusPainted( false );
			checkbox.setBorderPainted( true );

			checkbox.setBackground( isSelected ? getSelectionBackground() : getBackground() );
			checkbox.setForeground( isSelected ? getSelectionForeground() : getForeground() );

			checkbox.setEnabled( isEnabled() );
			checkbox.setOpaque( isSelected ); // Fixes Nimbus bug

			// Border according to isSelected
			Border highlightBorder = UIManager.getBorder( "List.focusCellHighlightBorder" );
			if( isSelected )
				checkbox.setBorder( highlightBorder );
			else
			{
				Insets insets = highlightBorder.getBorderInsets( checkbox );
				checkbox.setBorder( BorderFactory.createEmptyBorder( insets.top, insets.left, insets.bottom, insets.right ) );
			}

			return checkbox;
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;
}
package com.threecrickets.sincerity.eclipse.internal;

import java.util.ArrayList;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;

// See: http://www.eclipse.org/articles/Article-Field-Editors/field_editors.html

public abstract class PreferencePageWithFields extends PreferencePage
{
	//
	// PreferencePage
	//

	@Override
	public boolean performOk()
	{
		for( FieldEditor fieldEditor : fieldEditors )
			fieldEditor.store();
		return super.performOk();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected void performDefaults()
	{
		for( FieldEditor fieldEditor : fieldEditors )
			fieldEditor.loadDefault();
		super.performDefaults();
	}

	protected void addField( FieldEditor fieldEditor )
	{
		fieldEditors.add( fieldEditor );
		fieldEditor.setPage( this );
		fieldEditor.setPreferenceStore( getPreferenceStore() );
		fieldEditor.load();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private ArrayList<FieldEditor> fieldEditors = new ArrayList<FieldEditor>();
}

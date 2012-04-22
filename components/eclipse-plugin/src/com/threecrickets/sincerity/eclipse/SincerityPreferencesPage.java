package com.threecrickets.sincerity.eclipse;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class SincerityPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{
	//
	// Construction
	//

	public SincerityPreferencesPage()
	{
		super( GRID );
	}

	//
	// FieldEditorPreferencePage
	//

	@Override
	public void createFieldEditors()
	{
		addField( new DirectoryFieldEditor( SincerityPlugin.SINCERITY_HOME, Messages.DirLabel, getFieldEditorParent() ) );
	}

	//
	// IWorkbenchPreferencePage
	//

	public void init( IWorkbench workbench )
	{
		setPreferenceStore( SincerityPlugin.getDefault().getPreferenceStore() );
		setDescription( Messages.PageDesc );
	}
}

package com.threecrickets.sincerity.eclipse;

import java.util.ArrayList;

import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.IVMInstallType;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.threecrickets.sincerity.eclipse.internal.EclipseUtil;
import com.threecrickets.sincerity.eclipse.internal.PreferencePageWithFields;
import com.threecrickets.sincerity.eclipse.internal.Text;

public class SincerityPreferencesPage extends PreferencePageWithFields implements IWorkbenchPreferencePage, IPropertyChangeListener
{
	//
	// PreferencePage
	//

	@Override
	protected Control createContents( Composite parent )
	{
		Composite top = EclipseUtil.createComposite( parent, 1, 1, GridData.FILL_BOTH );
		
		Group sincerityGroup = EclipseUtil.createGroup( top, Text.PreferencesInstallation, 1, 1, GridData.FILL_HORIZONTAL );
		addField( new DirectoryFieldEditor( SincerityPlugin.SINCERITY_HOME_ATTRIBUTE, Text.PreferencesHome, EclipseUtil.createComposite( sincerityGroup ) ) );

		Group jreGroup = EclipseUtil.createGroup( top, Text.PreferencesJre, 1, 1, GridData.FILL_HORIZONTAL );
		BooleanFieldEditor useAlternateJre = new BooleanFieldEditor( SincerityPlugin.USE_EXTERNAL_JRE_ATTRIBUTE, Text.PreferencesUseExternalJre, EclipseUtil.createComposite( jreGroup ) );
		addField( useAlternateJre );
		addField( new ComboFieldEditor( SincerityPlugin.EXTERNAL_JRE_ATTRIBUTE, Text.PreferencesExternalJre, getJres(), alternateJreWrapper = EclipseUtil.createComposite( jreGroup ) ) );

		boolean enabled = useAlternateJre.getBooleanValue();
		for( Control child : alternateJreWrapper.getChildren() )
			child.setEnabled( enabled );

		useAlternateJre.setPropertyChangeListener( this );

		return top;
	}

	//
	// IWorkbenchPreferencePage
	//

	public void init( IWorkbench workbench )
	{
		setPreferenceStore( SincerityPlugin.getDefault().getPreferenceStore() );
	}

	//
	// IPropertyChangeListener
	//

	public void propertyChange( PropertyChangeEvent event )
	{
		boolean enabled = ( (Boolean) event.getNewValue() ).booleanValue();
		for( Control child : alternateJreWrapper.getChildren() )
			child.setEnabled( enabled );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private Composite alternateJreWrapper;

	private static String[][] getJres()
	{
		ArrayList<String[]> jres = new ArrayList<String[]>();
		for( IVMInstallType installType : JavaRuntime.getVMInstallTypes() )
		{
			for( IVMInstall install : installType.getVMInstalls() )
			{
				jres.add( new String[]
				{
					install.getName(), JavaRuntime.getCompositeIdFromVM( install )
				} );
			}
		}
		return jres.toArray( new String[jres.size()][] );
	}
}

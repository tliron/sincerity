package com.threecrickets.sincerity.eclipse.internal;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

public abstract class AbstractSincerityClasspathContainerPage extends WizardPage implements IClasspathContainerPage
{
	//
	// Construction
	//

	public AbstractSincerityClasspathContainerPage( IPath id, String name, String description )
	{
		super( name, name, null );
		setDescription( description );
		setPageComplete( true );
		this.id = id;
	}

	//
	// IClasspathContainerPage
	//

	public void createControl( Composite parent )
	{
		setControl( new Composite( parent, SWT.NULL ) );
	}

	public boolean finish()
	{
		return true;
	}

	public IClasspathEntry getSelection()
	{
		return JavaCore.newContainerEntry( id );
	}

	public void setSelection( IClasspathEntry containerEntry )
	{
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final IPath id;
}

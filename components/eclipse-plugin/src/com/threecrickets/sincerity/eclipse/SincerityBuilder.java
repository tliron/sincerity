package com.threecrickets.sincerity.eclipse;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class SincerityBuilder extends IncrementalProjectBuilder
{
	//
	// Constants
	//

	public static final String ID = "com.threecrickets.sincerity.builder";

	//
	// IncrementalProjectBuilder
	//

	@Override
	protected IProject[] build( int kind, Map<String, String> args, IProgressMonitor progressMonitor ) throws CoreException
	{
		return null;
	}

	@Override
	protected void startupOnInitialize()
	{
	}

	@Override
	protected void clean( IProgressMonitor monitor )
	{
	}
}

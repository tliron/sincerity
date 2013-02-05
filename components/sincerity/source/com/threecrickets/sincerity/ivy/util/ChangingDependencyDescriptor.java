/**
 * Copyright 2011-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.sincerity.ivy.util;

import java.util.Map;

import org.apache.ivy.core.module.descriptor.DependencyArtifactDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.IncludeRule;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.plugins.namespace.Namespace;

/**
 * Wraps a {@link DependencyDescriptor} while making sure it always returns true
 * for {@link DependencyDescriptor#isChanging()}.
 * <p>
 * Currently unused code!
 * 
 * @author Tal Liron
 */
public class ChangingDependencyDescriptor implements DependencyDescriptor
{
	//
	// Construction
	//

	public ChangingDependencyDescriptor( DependencyDescriptor dd )
	{
		this.dependencyDescriptor = dd;
	}

	//
	// DependencyDescriptor
	//

	public String getAttribute( String attName )
	{
		return dependencyDescriptor.getAttribute( attName );
	}

	public String getExtraAttribute( String attName )
	{
		return dependencyDescriptor.getExtraAttribute( attName );
	}

	@SuppressWarnings("rawtypes")
	public Map getAttributes()
	{
		return dependencyDescriptor.getAttributes();
	}

	@SuppressWarnings("rawtypes")
	public Map getExtraAttributes()
	{
		return dependencyDescriptor.getExtraAttributes();
	}

	@SuppressWarnings("rawtypes")
	public Map getQualifiedExtraAttributes()
	{
		return dependencyDescriptor.getQualifiedExtraAttributes();
	}

	public ModuleRevisionId getSourceModule()
	{
		return dependencyDescriptor.getSourceModule();
	}

	public ModuleId getDependencyId()
	{
		return dependencyDescriptor.getDependencyId();
	}

	public boolean isForce()
	{
		return dependencyDescriptor.isForce();
	}

	public boolean isChanging()
	{
		// This is the only value we don't delegate to the wrapped descriptor
		return true;
	}

	public boolean isTransitive()
	{
		return dependencyDescriptor.isTransitive();
	}

	public ModuleRevisionId getParentRevisionId()
	{
		return dependencyDescriptor.getParentRevisionId();
	}

	public ModuleRevisionId getDependencyRevisionId()
	{
		return dependencyDescriptor.getDependencyRevisionId();
	}

	public ModuleRevisionId getDynamicConstraintDependencyRevisionId()
	{
		return dependencyDescriptor.getDynamicConstraintDependencyRevisionId();
	}

	public String[] getModuleConfigurations()
	{
		return dependencyDescriptor.getModuleConfigurations();
	}

	public String[] getDependencyConfigurations( String moduleConfiguration, String requestedConfiguration )
	{
		return dependencyDescriptor.getDependencyConfigurations( moduleConfiguration, requestedConfiguration );
	}

	public String[] getDependencyConfigurations( String moduleConfiguration )
	{
		return dependencyDescriptor.getDependencyConfigurations( moduleConfiguration );
	}

	public String[] getDependencyConfigurations( String[] moduleConfigurations )
	{
		return dependencyDescriptor.getDependencyConfigurations( moduleConfigurations );
	}

	public Namespace getNamespace()
	{
		return dependencyDescriptor.getNamespace();
	}

	public DependencyArtifactDescriptor[] getAllDependencyArtifacts()
	{
		return dependencyDescriptor.getAllDependencyArtifacts();
	}

	public DependencyArtifactDescriptor[] getDependencyArtifacts( String moduleConfigurations )
	{
		return dependencyDescriptor.getDependencyArtifacts( moduleConfigurations );
	}

	public DependencyArtifactDescriptor[] getDependencyArtifacts( String[] moduleConfigurations )
	{
		return dependencyDescriptor.getDependencyArtifacts( moduleConfigurations );
	}

	public IncludeRule[] getAllIncludeRules()
	{
		return dependencyDescriptor.getAllIncludeRules();
	}

	public IncludeRule[] getIncludeRules( String moduleConfigurations )
	{
		return dependencyDescriptor.getIncludeRules( moduleConfigurations );
	}

	public IncludeRule[] getIncludeRules( String[] moduleConfigurations )
	{
		return dependencyDescriptor.getIncludeRules( moduleConfigurations );
	}

	public ExcludeRule[] getAllExcludeRules()
	{
		return dependencyDescriptor.getAllExcludeRules();
	}

	public ExcludeRule[] getExcludeRules( String moduleConfigurations )
	{
		return dependencyDescriptor.getExcludeRules( moduleConfigurations );
	}

	public ExcludeRule[] getExcludeRules( String[] moduleConfigurations )
	{
		return dependencyDescriptor.getExcludeRules( moduleConfigurations );
	}

	public boolean doesExclude( String[] moduleConfigurations, ArtifactId artifactId )
	{
		return dependencyDescriptor.doesExclude( moduleConfigurations, artifactId );
	}

	public boolean canExclude()
	{
		return dependencyDescriptor.canExclude();
	}

	public DependencyDescriptor asSystem()
	{
		return dependencyDescriptor.asSystem();
	}

	public DependencyDescriptor clone( ModuleRevisionId revision )
	{
		return dependencyDescriptor.clone( revision );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final DependencyDescriptor dependencyDescriptor;
}
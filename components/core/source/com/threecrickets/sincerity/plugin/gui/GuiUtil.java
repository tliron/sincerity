package com.threecrickets.sincerity.plugin.gui;

import java.io.File;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.License;
import org.apache.ivy.core.module.id.ModuleRevisionId;

import com.threecrickets.sincerity.Dependencies;
import com.threecrickets.sincerity.ResolvedDependency;

public class GuiUtil
{
	public static void expandTree( JTree tree, boolean expand )
	{
		expandAll( tree, new TreePath( (TreeNode) tree.getModel().getRoot() ), expand );
	}

	public static String toHtml( ResolvedDependency resolvedDependency, boolean br )
	{
		ModuleRevisionId id = resolvedDependency.descriptor.getModuleRevisionId();
		String organisation = id.getOrganisation();
		String name = id.getName();
		String revision = id.getRevision();

		StringBuilder s = new StringBuilder();
		if( resolvedDependency.evicted != null )
			s.append( "<i>" );
		s.append( "<b>" );
		s.append( organisation );
		if( !name.equals( organisation ) )
		{
			s.append( ':' );
			s.append( name );
		}
		s.append( "</b>" );
		if( !"latest.integration".equals( revision ) )
		{
			s.append( br ? "<br/>v" : " v" );
			s.append( revision );
		}
		if( resolvedDependency.evicted != null )
			s.append( "</i>" );
		return s.toString();
	}

	public static void addDependency( ResolvedDependency resolvedDependency, DefaultMutableTreeNode parent, Dependencies dependencies, boolean recursive, boolean licenses, boolean artifacts )
	{
		DefaultMutableTreeNode node = new DefaultMutableTreeNode( "<html>" + toHtml( resolvedDependency, false ) + "</html>" );
		parent.add( node );

		if( licenses )
			addLicenses( resolvedDependency, node );

		if( artifacts )
			addArtifacts( resolvedDependency, node, dependencies );

		if( recursive )
			for( ResolvedDependency child : resolvedDependency.children )
				addDependency( child, node, dependencies, recursive, licenses, artifacts );
	}

	public static void addLicenses( ResolvedDependency resolvedDependency, DefaultMutableTreeNode parent )
	{
		for( License license : resolvedDependency.descriptor.getLicenses() )
		{
			StringBuilder s = new StringBuilder();
			s.append( "<html><a href=\"" );
			s.append( license.getUrl() );
			s.append( "\">" );
			s.append( license.getName() );
			s.append( "</a>" );
			s.append( "</html>" );

			parent.add( new DefaultMutableTreeNode( s.toString() ) );
		}
	}

	public static void addArtifacts( ResolvedDependency resolvedDependency, DefaultMutableTreeNode parent, Dependencies dependencies )
	{
		for( Artifact artifact : resolvedDependency.descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION ) )
		{
			StringBuilder s = new StringBuilder();

			String location = artifact.getId().getAttribute( "location" );
			boolean installed = location != null && new File( location ).exists();

			s.append( "<html>" );
			if( !installed )
				s.append( "<i>" );

			String size = artifact.getId().getAttribute( "size" );
			if( location != null )
				s.append( dependencies.getContainer().getRelativePath( location ) );
			else
			{
				// Could not find a location for it?
				s.append( artifact.getName() );
				s.append( '.' );
				s.append( artifact.getExt() );
				s.append( '?' );
			}
			s.append( " (" );
			s.append( artifact.getType() );
			if( size != null )
			{
				s.append( ", " );
				s.append( size );
				s.append( " bytes" );
			}
			s.append( ')' );

			if( !installed )
				s.append( "</i>" );
			s.append( "</html>" );

			parent.add( new DefaultMutableTreeNode( s.toString() ) );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static void expandAll( JTree tree, TreePath parent, boolean expand )
	{
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if( node.getChildCount() >= 0 )
		{
			for( Enumeration<?> e = node.children(); e.hasMoreElements(); )
			{
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild( n );
				expandAll( tree, path, expand );
			}
		}

		if( expand )
			tree.expandPath( parent );
		else
			tree.collapsePath( parent );
	}
}

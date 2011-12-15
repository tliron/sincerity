package com.threecrickets.sincerity.plugin.gui;

import java.io.File;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
	public static void setNativeLookAndFeel()
	{
		// System.setProperty( "awt.useSystemAAFontSettings", "on" );
		String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
		try
		{
			UIManager.setLookAndFeel( lookAndFeel );
		}
		catch( InstantiationException x )
		{
			x.printStackTrace();
		}
		catch( ClassNotFoundException x )
		{
			x.printStackTrace();
		}
		catch( UnsupportedLookAndFeelException x )
		{
			x.printStackTrace();
		}
		catch( IllegalAccessException x )
		{
			x.printStackTrace();
		}
	}

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
			for( License license : resolvedDependency.descriptor.getLicenses() )
				addLicense( license, node );

		if( artifacts )
			for( Artifact artifact : resolvedDependency.descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION ) )
				addArtifact( artifact, node, dependencies );

		if( recursive )
			for( ResolvedDependency child : resolvedDependency.children )
				addDependency( child, node, dependencies, true, licenses, artifacts );
	}

	public static void addLicense( License license, DefaultMutableTreeNode parent )
	{
		StringBuilder s = new StringBuilder();
		s.append( "<html>" );
		s.append( license.getName() );
		s.append( ": " );
		s.append( license.getUrl() );
		s.append( "</html>" );

		parent.add( new DefaultMutableTreeNode( s.toString() ) );
	}

	public static void addArtifact( Artifact artifact, DefaultMutableTreeNode parent, Dependencies dependencies )
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

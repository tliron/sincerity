package com.threecrickets.sincerity.plugin.gui;

import java.io.File;
import java.util.Enumeration;

import javax.swing.ImageIcon;
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
import com.threecrickets.sincerity.Package;
import com.threecrickets.sincerity.ResolvedDependency;
import com.threecrickets.sincerity.exception.SincerityException;

public class GuiUtil
{
	public static final ImageIcon DEPENDENCY_ICON = new ImageIcon( GuiUtil.class.getResource( "cog.png" ) );

	public static final ImageIcon LICENSE_ICON = new ImageIcon( GuiUtil.class.getResource( "book.png" ) );

	public static final ImageIcon FOLDER_ICON = new ImageIcon( GuiUtil.class.getResource( "folder.png" ) );

	public static final ImageIcon FILE_ICON = new ImageIcon( GuiUtil.class.getResource( "database.png" ) );

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

	public static void error( Throwable x )
	{
	}

	public static void expandTree( JTree tree, boolean expand )
	{
		expandAll( tree, new TreePath( (TreeNode) tree.getModel().getRoot() ), expand );
	}

	public static String toHtml( ResolvedDependency resolvedDependency, boolean bold, boolean br )
	{
		ModuleRevisionId id = resolvedDependency.descriptor.getModuleRevisionId();
		String organisation = id.getOrganisation();
		String name = id.getName();
		String revision = id.getRevision();

		StringBuilder s = new StringBuilder();
		s.append( "<html>" );
		if( resolvedDependency.evicted != null )
			s.append( "<i>" );
		if( bold )
			s.append( "<b>" );
		s.append( organisation );
		if( !name.equals( organisation ) )
		{
			s.append( ':' );
			s.append( name );
		}
		if( bold )
			s.append( "</b>" );
		if( !"latest.integration".equals( revision ) )
		{
			s.append( br ? "<br/>v" : " v" );
			s.append( revision );
		}
		if( resolvedDependency.evicted != null )
			s.append( "</i>" );
		s.append( "</html>" );
		return s.toString();
	}

	public static DefaultMutableTreeNode createDependencyNode( ResolvedDependency resolvedDependency, Dependencies dependencies, boolean isMain, boolean includeChildren, boolean includeLicenses,
		boolean includeArtifacts, boolean includePackageContents ) throws SincerityException
	{
		EnhancedNode node = new EnhancedNode( resolvedDependency, toHtml( resolvedDependency, isMain, false ), GuiUtil.DEPENDENCY_ICON );

		if( includeLicenses )
			for( License license : resolvedDependency.descriptor.getLicenses() )
				node.add( createLicenseNode( license, dependencies, false, false, false, false ) );

		if( includeArtifacts )
			for( Artifact artifact : resolvedDependency.descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION ) )
				node.add( createArtifactNode( artifact, dependencies, includePackageContents ) );

		if( includeChildren )
			for( ResolvedDependency child : resolvedDependency.children )
				node.add( createDependencyNode( child, dependencies, isMain, true, includeLicenses, includeArtifacts, includePackageContents ) );

		return node;
	}

	public static DefaultMutableTreeNode createLicenseNode( License license, Dependencies dependencies, boolean isMain, boolean includeDependencies, boolean includeArtifacts, boolean includePackageContents )
		throws SincerityException
	{
		StringBuilder s = new StringBuilder();
		s.append( "<html>" );
		if( isMain )
			s.append( "<b>" );
		s.append( license.getName() );
		s.append( ": " );
		s.append( license.getUrl() );
		if( isMain )
			s.append( "</b>" );
		s.append( "</html>" );

		EnhancedNode node = new EnhancedNode( license, s.toString(), GuiUtil.LICENSE_ICON );

		if( includeDependencies )
			for( ResolvedDependency resolvedDependency : dependencies.getResolvedDependencies().getByLicense( license ) )
				node.add( createDependencyNode( resolvedDependency, dependencies, false, false, false, includeArtifacts, includePackageContents ) );
		else if( includeArtifacts )
		{
			for( ResolvedDependency resolvedDependency : dependencies.getResolvedDependencies().getByLicense( license ) )
				for( Artifact artifact : resolvedDependency.descriptor.getArtifacts( DefaultModuleDescriptor.DEFAULT_CONFIGURATION ) )
					node.add( createArtifactNode( artifact, dependencies, includePackageContents ) );
		}

		return node;
	}

	public static DefaultMutableTreeNode createArtifactNode( Artifact artifact, Dependencies dependencies, boolean includePackageContents ) throws SincerityException
	{
		StringBuilder s = new StringBuilder();

		String location = artifact.getId().getAttribute( "location" );
		boolean installed = location != null && new File( location ).exists();

		s.append( "<html>" );
		if( !installed )
			s.append( "<i>" );

		String size = artifact.getId().getAttribute( "size" );
		if( location != null )
		{
			location = dependencies.getContainer().getRelativePath( location );
			s.append( location );
		}
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

		EnhancedNode node = new EnhancedNode( artifact, s.toString(), GuiUtil.FILE_ICON );

		if( location != null && includePackageContents )
		{
			Package pack = dependencies.getPackages().getByPackage( new File( location ) );
			if( pack != null )
			{
				for( com.threecrickets.sincerity.Artifact packedArtifact : pack )
					addFileNode( dependencies.getContainer().getRelativeFile( packedArtifact.getFile() ), GuiUtil.FILE_ICON, node );
			}
		}

		return node;
	}

	private static EnhancedNode findFileNode( File file, EnhancedNode from, EnhancedNode root )
	{
		if( file.equals( from.getUserObject() ) )
			return from;

		for( Enumeration<?> e = from.children(); e.hasMoreElements(); )
		{
			EnhancedNode node = (EnhancedNode) e.nextElement();
			EnhancedNode found = findFileNode( file, node, root );
			if( found != null )
				return found;
		}

		return null;
	}

	private static EnhancedNode addFileNode( File file, ImageIcon icon, EnhancedNode root )
	{
		File parent = file.getParentFile();
		EnhancedNode parentNode = null;
		if( parent == null )
			parentNode = root;
		else
		{
			parentNode = findFileNode( parent, root, root );
			if( parentNode == null )
				parentNode = addFileNode( parent, GuiUtil.FOLDER_ICON, root );
		}
		EnhancedNode node = new EnhancedNode( file, file.getName(), icon );
		parentNode.add( node );
		return node;
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

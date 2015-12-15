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

package com.threecrickets.sincerity.plugin.swing;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.File;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import com.threecrickets.bootstrap.Bootstrap;
import com.threecrickets.sincerity.Command;
import com.threecrickets.sincerity.Plugin1;
import com.threecrickets.sincerity.Shortcuts;
import com.threecrickets.sincerity.Sincerity;
import com.threecrickets.sincerity.Template;
import com.threecrickets.sincerity.dependencies.Artifact;
import com.threecrickets.sincerity.dependencies.Dependencies;
import com.threecrickets.sincerity.dependencies.License;
import com.threecrickets.sincerity.dependencies.Module;
import com.threecrickets.sincerity.dependencies.Repositories;
import com.threecrickets.sincerity.dependencies.Repository;
import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.packaging.Package;
import com.threecrickets.sincerity.util.swing.EnhancedNode;

/**
 * General Swing utilities for Sincerity.
 * 
 * @author Tal Liron
 */
public class SwingUtil
{
	public static final ImageIcon DEPENDENCY_ICON = new ImageIcon( SwingUtil.class.getResource( "cog.png" ) );

	public static final ImageIcon LICENSE_ICON = new ImageIcon( SwingUtil.class.getResource( "book.png" ) );

	public static final ImageIcon FOLDER_ICON = new ImageIcon( SwingUtil.class.getResource( "folder.png" ) );

	public static final ImageIcon FILE_ICON = new ImageIcon( SwingUtil.class.getResource( "database.png" ) );

	public static final ImageIcon PACKAGE_ICON = new ImageIcon( SwingUtil.class.getResource( "database_add.png" ) );

	public static final ImageIcon PLUGIN_ICON = new ImageIcon( SwingUtil.class.getResource( "plugin.png" ) );

	public static final ImageIcon COMMAND_ICON = new ImageIcon( SwingUtil.class.getResource( "control_play.png" ) );

	public static final String GTK_LOOK_AND_FEEL = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";

	public static final String LOOK_AND_FEEL_ATTRIBUTE = "com.threecrickets.sincerity.lookAndFeel";

	public static String getLookAndFeel( String name )
	{
		if( !"native".equals( name ) )
			for( UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() )
				if( name.equals( info.getName().toLowerCase() ) )
					return info.getClassName();
		return null;
	}

	public static void initLookAndFeel( String ui ) throws SincerityException
	{
		String current = (String) Bootstrap.getAttributes().get( LOOK_AND_FEEL_ATTRIBUTE );
		if( ( current == null ) || !current.equals( ui ) )
		{
			String lookAndFeel = getLookAndFeel( ui );

			if( lookAndFeel == null )
			{
				// Default to native
				lookAndFeel = UIManager.getSystemLookAndFeelClassName();

				// The GTK L&F is so broken that we will prefer anything else!
				if( GTK_LOOK_AND_FEEL.equals( lookAndFeel ) )
				{
					lookAndFeel = getLookAndFeel( "nimbus" );
					if( lookAndFeel == null )
						lookAndFeel = getLookAndFeel( "metal" );
				}
			}

			if( GTK_LOOK_AND_FEEL.equals( lookAndFeel ) )
			{
				// See: http://bugs.sun.com/view_bug.do?bug_id=6742850
				System.setProperty( "awt.useSystemAAFontSettings", "lcd" );
				System.setProperty( "swing.aatext", "on" );
			}

			JFrame.setDefaultLookAndFeelDecorated( true );
			JDialog.setDefaultLookAndFeelDecorated( true );

			try
			{
				UIManager.setLookAndFeel( lookAndFeel );
			}
			catch( InstantiationException x )
			{
				throw new SincerityException( "Could not set look-and-feel to: " + ui, x );
			}
			catch( ClassNotFoundException x )
			{
				throw new SincerityException( "Could not set look-and-feel to: " + ui, x );
			}
			catch( UnsupportedLookAndFeelException x )
			{
				throw new SincerityException( "Could not set look-and-feel to: " + ui, x );
			}
			catch( IllegalAccessException x )
			{
				throw new SincerityException( "Could not set look-and-feel to: " + ui, x );
			}

			Bootstrap.getAttributes().put( LOOK_AND_FEEL_ATTRIBUTE, ui );
		}
	}

	public static void error( Throwable x )
	{
		Sincerity.getCurrent().dumpStackTrace( x );
	}

	public static void center( Window window, double ratio )
	{
		Rectangle bounds = window.getGraphicsConfiguration().getBounds();
		int width = bounds.width / 2;
		int height = bounds.height / 2;
		int centerX = (int) ( bounds.x + bounds.width * ratio );
		int centerY = (int) ( bounds.y + bounds.height * ratio );
		window.setLocation( centerX - width / 2, centerY - height / 2 );
		window.setPreferredSize( new Dimension( width, height ) );
		window.pack();
	}

	public static void center( Window window )
	{
		window.pack();
		Rectangle bounds = window.getGraphicsConfiguration().getBounds();
		int width = window.getWidth();
		int height = window.getHeight();
		int centerX = bounds.x + bounds.width / 2;
		int centerY = bounds.y + bounds.height / 2;
		window.setLocation( centerX - width / 2, centerY - height / 2 );
		window.setPreferredSize( new Dimension( width, height ) );
	}

	public static void expandTree( JTree tree, boolean expand )
	{
		expandAll( tree, new TreePath( (TreeNode) tree.getModel().getRoot() ), expand );
	}

	public static String toHtml( Module module, boolean bold, boolean br )
	{
		String group = module.getGroup();
		String name = module.getName();
		String version = module.getVersion();

		StringBuilder s = new StringBuilder();
		s.append( "<html>" );
		if( module.isEvicted() )
			s.append( "<i>" );
		if( bold )
			s.append( "<b>" );
		s.append( group );
		if( !name.equals( group ) )
		{
			s.append( ':' );
			s.append( name );
		}
		if( bold )
			s.append( "</b>" );
		if( !"latest.integration".equals( version ) )
		{
			s.append( br ? "<br/>v" : " v" );
			s.append( version );
		}
		if( module.isEvicted() )
			s.append( "</i>" );
		s.append( "</html>" );
		return s.toString();
	}

	public static EnhancedNode createDependencyNode( Module module, Dependencies<?> dependencies, boolean isMain, boolean includeChildren, boolean includeLicenses, boolean includeArtifacts,
		boolean includePackageContents ) throws SincerityException
	{
		EnhancedNode node = new EnhancedNode( module, toHtml( module, isMain, false ), DEPENDENCY_ICON );

		if( includeLicenses )
			for( License license : module.getLicenses() )
				node.add( createLicenseNode( license, dependencies, false, false, false, false ) );

		if( includeArtifacts )
			for( Artifact artifact : module.getArtifacts() )
				node.add( createArtifactNode( artifact, dependencies, includePackageContents ) );

		if( includeChildren )
			for( Module child : module.getChildren() )
				node.add( createDependencyNode( child, dependencies, isMain, true, includeLicenses, includeArtifacts, includePackageContents ) );

		return node;
	}

	public static EnhancedNode createLicenseNode( License license, Dependencies<?> dependencies, boolean isMain, boolean includeDependencies, boolean includeArtifacts, boolean includePackageContents )
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

		EnhancedNode node = new EnhancedNode( license, s.toString(), LICENSE_ICON );

		if( includeDependencies )
			for( Module module : dependencies.getModules().getByLicense( license ) )
				node.add( createDependencyNode( module, dependencies, false, false, false, includeArtifacts, includePackageContents ) );
		else if( includeArtifacts )
		{
			for( Module module : dependencies.getModules().getByLicense( license ) )
				for( Artifact artifact : module.getArtifacts() )
					node.add( createArtifactNode( artifact, dependencies, includePackageContents ) );
		}

		return node;
	}

	public static EnhancedNode createArtifactNode( Artifact artifact, Dependencies<?> dependencies, boolean includePackageContents ) throws SincerityException
	{
		StringBuilder s = new StringBuilder();

		File location = artifact.getLocation();
		boolean installed = location != null && location.exists();

		s.append( "<html>" );
		if( !installed )
			s.append( "<i>" );

		Integer size = artifact.getSize();
		if( location != null )
		{
			location = dependencies.getContainer().getRelativeFile( location );
			s.append( location );
		}
		else
		{
			// Could not find a location for it?
			s.append( artifact.getName() );
			s.append( '.' );
			s.append( artifact.getExtension() );
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

		Package pack = location != null ? dependencies.getPackages().getPackage( location ) : null;
		EnhancedNode node = new EnhancedNode( artifact, s.toString(), pack != null ? PACKAGE_ICON : FILE_ICON );

		if( includePackageContents && pack != null )
		{
			for( com.threecrickets.sincerity.packaging.Artifact packedArtifact : pack )
				addFileNode( dependencies.getContainer().getRelativeFile( packedArtifact.getFile() ), FILE_ICON, node );
		}

		return node;
	}

	public static EnhancedNode createPluginNode( Plugin1 plugin, boolean includeCommands ) throws SincerityException
	{
		EnhancedNode node = new EnhancedNode( plugin, "<html><b>" + plugin.getName() + "</b></html>", PLUGIN_ICON );

		if( includeCommands )
			for( String command : plugin.getCommands() )
				node.add( createCommandNode( command, plugin, false ) );

		return node;
	}

	public static EnhancedNode createCommandNode( String command, Plugin1 plugin, boolean includePluginName ) throws SincerityException
	{
		String full = plugin.getName() + Command.PLUGIN_COMMAND_SEPARATOR + command;
		return new EnhancedNode( full, includePluginName ? full : command, COMMAND_ICON );
	}

	public static EnhancedNode createShortcutTypeNode( String type, Shortcuts shortcuts ) throws SincerityException
	{
		EnhancedNode node = new EnhancedNode( type, "<html><b>" + type + "</b></html>", PLUGIN_ICON );

		if( shortcuts != null )
			for( String shortcut : shortcuts.getByType( type ) )
				node.add( createShortcutNode( shortcut, false ) );

		return node;
	}

	public static EnhancedNode createShortcutNode( String shortcut, boolean includeType ) throws SincerityException
	{
		if( !includeType && shortcut.contains( Shortcuts.SHORTCUT_TYPE_SEPARATOR ) )
			return new EnhancedNode( shortcut, shortcut.substring( shortcut.indexOf( Shortcuts.SHORTCUT_TYPE_SEPARATOR ) + Shortcuts.SHORTCUT_TYPE_SEPARATOR.length() ), COMMAND_ICON );
		else
			return new EnhancedNode( shortcut, shortcut, COMMAND_ICON );
	}

	public static EnhancedNode createTemplateNode( Template template ) throws SincerityException
	{
		return new EnhancedNode( template, template.getName(), FILE_ICON );
	}

	public static EnhancedNode createProgramNode( String program ) throws SincerityException
	{
		return new EnhancedNode( program, program, COMMAND_ICON );
	}

	public static EnhancedNode createRepositoryNode( Repository repository ) throws SincerityException
	{
		StringBuilder s = new StringBuilder();
		s.append( "<html><b>" );

		String name = repository.getName();
		String[] split = name.split( Repositories.REPOSITORY_SECTION_SEPARATOR, 2 );
		if( split.length == 2 )
			s.append( split[1] );
		else
			s.append( name );
		s.append( "</b>" );

		s.append( ": " );
		s.append( repository.getType() );
		s.append( ":" );
		s.append( repository.getLocation() );

		s.append( "</html>" );
		return new EnhancedNode( repository, s.toString(), FILE_ICON );
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
				parentNode = addFileNode( parent, FOLDER_ICON, root );
		}
		EnhancedNode node = new EnhancedNode( file, file.getName(), icon );
		parentNode.add( node );
		return node;
	}
}

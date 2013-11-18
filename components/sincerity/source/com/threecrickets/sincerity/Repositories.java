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

package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.settings.XmlSettingsParser;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.DependencyResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.threecrickets.sincerity.exception.SincerityException;
import com.threecrickets.sincerity.internal.XmlUtil;
import com.threecrickets.sincerity.ivy.pypi.PyPiResolver;

/**
 * Manages the source repositories for a {@link Container}.
 * <p>
 * Repositories can be organized in "sections", allowing to prioritize the list
 * according to these groups. For example, a common scheme is to have two
 * sections, "public" and "private".
 * <p>
 * The Ivy configuration is stored in
 * "/configuration/sincerity/repositories.conf". For low-level access to the Ivy
 * resolvers, see {@link #getResolvers(String)}.
 * 
 * @author Tal Liron
 * @see Container#getRepositories()
 */
public class Repositories
{
	//
	// Constants
	//

	public static final String REPOSITORY_SECTION_SEPARATOR = ":";

	//
	// Construction
	//

	/**
	 * Parses the Ivy settings file. Note that only the "resolvers" section will
	 * be taken into account.
	 * 
	 * @param ivyFile
	 *        The Ivy settings file (usually
	 *        "/configuration/sincerity/repositories.conf")
	 * @param ivy
	 *        The Ivy instance
	 * @throws SincerityException
	 *         In case of an error
	 */
	public Repositories( File ivyFile, Ivy ivy ) throws SincerityException
	{
		this.ivyFile = ivyFile;
		this.ivy = ivy;

		if( ivyFile.exists() )
		{
			try
			{
				new XmlSettingsParser( ivy.getSettings() ).parse( ivyFile.toURI().toURL() );
			}
			catch( MalformedURLException x )
			{
				throw new RuntimeException( x );
			}
			catch( ParseException x )
			{
				throw new SincerityException( "Could not parse repositories configuration", x );
			}
			catch( IOException x )
			{
				throw new SincerityException( "Could not read repositories configuration", x );
			}
		}

		// Add resolvers to chains
		for( Object r : ivy.getSettings().getResolvers() )
		{
			DependencyResolver resolver = (DependencyResolver) r;
			String name = resolver.getName();
			String[] names = name.split( REPOSITORY_SECTION_SEPARATOR, 2 );
			if( names.length > 1 )
				addResolver( names[0], resolver, false );
		}
	}

	//
	// Attributes
	//

	/**
	 * The resolvers in a section.
	 * 
	 * @param section
	 *        The section name
	 * @return The resolvers or null
	 */
	@SuppressWarnings("unchecked")
	public Collection<DependencyResolver> getResolvers( String section )
	{
		DependencyResolver chain = ivy.getSettings().getResolver( section );
		if( chain instanceof ChainResolver )
			return ( (ChainResolver) chain ).getResolvers();
		return null;
	}

	//
	// Operations
	//

	/**
	 * Adds a Maven resolver.
	 * 
	 * @param section
	 *        The section name
	 * @param name
	 *        The resolver name within the section
	 * @param url
	 *        The Maven root URL
	 * @return True if added
	 * @throws SincerityException
	 *         In case of an error
	 */
	public boolean addMaven( String section, String name, String url ) throws SincerityException
	{
		name = section + REPOSITORY_SECTION_SEPARATOR + name;
		if( ivy.getSettings().getResolver( name ) != null )
			return false;

		IBiblioResolver resolver = new IBiblioResolver();
		resolver.setSettings( ivy.getSettings() );
		resolver.setEventManager( ivy.getEventManager() );
		resolver.setName( name );
		resolver.setM2compatible( true );
		resolver.setCheckmodified( true );
		resolver.setChecksums( "none" );
		if( url != null )
			resolver.setRoot( url );
		boolean added = addResolver( section, resolver, true );

		if( added )
		{
			try
			{
				Appender appender = new Appender( "ibiblio" );
				appender.element.setAttribute( "name", name );
				if( url != null )
					appender.element.setAttribute( "root", url );
				appender.element.setAttribute( "m2compatible", "true" );
				appender.element.setAttribute( "checkmodified", "true" );
				appender.element.setAttribute( "checksums", "none" );
				appender.save();
			}
			catch( Exception x )
			{
				throw new SincerityException( "Could not append to repositories configuration", x );
			}
		}

		return added;
	}

	/**
	 * Adds a PyPI resolver.
	 * 
	 * @param section
	 *        The section name
	 * @param name
	 *        The resolver name within the section
	 * @param url
	 *        The PyPI root URL
	 * @return True if added
	 * @throws SincerityException
	 *         In case of an error
	 */
	public boolean addPyPi( String section, String name, String url ) throws SincerityException
	{
		name = section + REPOSITORY_SECTION_SEPARATOR + name;
		if( ivy.getSettings().getResolver( name ) != null )
			return false;

		PyPiResolver resolver = new PyPiResolver();
		resolver.setSettings( ivy.getSettings() );
		resolver.setEventManager( ivy.getEventManager() );
		resolver.setName( name );
		resolver.setCheckmodified( true );
		resolver.setChecksums( "none" );
		if( url != null )
			resolver.setRoot( url );
		boolean added = addResolver( section, resolver, true );

		if( added )
		{
			try
			{
				Appender appender = new Appender( "pypi" );
				appender.element.setAttribute( "name", name );
				if( url != null )
					appender.element.setAttribute( "root", url );
				appender.element.setAttribute( "checkmodified", "true" );
				appender.element.setAttribute( "checksums", "none" );
				appender.save();
			}
			catch( Exception x )
			{
				throw new SincerityException( "Could not append to repositories configuration", x );
			}
		}

		return added;
	}

	/**
	 * Removes a resolver.
	 * 
	 * @param section
	 *        The section name
	 * @param name
	 *        The resolver name within the section
	 * @return True if removed
	 * @throws SincerityException
	 *         In case of an error
	 */
	public boolean remove( String section, String name ) throws SincerityException
	{
		name = section + REPOSITORY_SECTION_SEPARATOR + name;
		if( ivy.getSettings().getResolver( name ) == null )
			return false;

		boolean removed = removeResolver( section, name );

		if( removed )
		{
			if( ivyFile.exists() )
			{
				try
				{
					DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
					DocumentBuilder documentBuilder = factory.newDocumentBuilder();
					Document document = documentBuilder.parse( ivyFile );
					Element root = document.getDocumentElement();
					if( "ivysettings".equals( root.getTagName() ) )
					{
						NodeList resolversList = root.getElementsByTagName( "resolvers" );
						if( resolversList.getLength() > 0 )
						{
							Element resolvers = (Element) resolversList.item( 0 );
							if( resolvers != null )
							{
								resolversList = resolvers.getChildNodes();
								for( int i = 0, length = resolversList.getLength(); i < length; i++ )
								{
									Node child = resolversList.item( i );
									if( child.getNodeType() == Node.ELEMENT_NODE )
									{
										Element resolver = (Element) child;
										if( name.equals( resolver.getAttribute( "name" ) ) )
										{
											resolvers.removeChild( resolver );
											XmlUtil.removeTextNodes( root );
											XmlUtil.saveHumanReadable( document, ivyFile );
											break;
										}
									}
								}
							}
						}
					}
				}
				catch( Exception x )
				{
					throw new SincerityException( "Could not remove from repositories configuration", x );
				}
			}
		}

		return removed;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File ivyFile;

	private final Ivy ivy;

	/**
	 * Adds a resolver.
	 * 
	 * @param section
	 *        The section name
	 * @param resolver
	 *        The resolver
	 * @param root
	 *        True to add a root resolver as well as the section resolver
	 * @return True if added
	 */
	private boolean addResolver( String section, DependencyResolver resolver, boolean root )
	{
		DependencyResolver chain = ivy.getSettings().getResolver( section );
		if( chain instanceof ChainResolver )
		{
			( (ChainResolver) chain ).add( resolver );
			if( root )
				ivy.getSettings().addResolver( resolver );
			return true;
		}

		return false;
	}

	/**
	 * Removes a resolver.
	 * 
	 * @param section
	 *        The section name
	 * @param name
	 *        The resolver name
	 * @return True if removed
	 */
	private boolean removeResolver( String section, String name )
	{
		DependencyResolver chain = ivy.getSettings().getResolver( section );
		if( chain instanceof ChainResolver )
		{
			@SuppressWarnings("unchecked")
			List<DependencyResolver> resolvers = ( (ChainResolver) chain ).getResolvers();
			for( DependencyResolver existingResolver : resolvers )
			{
				if( name.equals( existingResolver.getName() ) )
				{
					resolvers.remove( existingResolver );
					ivy.getSettings().getResolvers().remove( existingResolver );
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Utility class to append a resolver to the Ivy settings file and save it
	 * as human-readable XML.
	 */
	private class Appender
	{
		public Appender( String type ) throws ParserConfigurationException, SAXException, IOException
		{
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();

			if( ivyFile.exists() )
			{
				document = documentBuilder.parse( ivyFile );
				Element root = document.getDocumentElement();
				Node resolvers = null;
				if( "ivysettings".equals( root.getTagName() ) )
				{
					NodeList resolversList = root.getElementsByTagName( "resolvers" );
					if( resolversList.getLength() > 0 )
						resolvers = resolversList.item( 0 );
				}

				if( resolvers != null )
				{
					element = document.createElement( type );
					resolvers.appendChild( element );
					XmlUtil.removeTextNodes( root );
				}
			}

			if( element == null )
			{
				document = documentBuilder.newDocument();
				Comment comment = document.createComment( XmlUtil.COMMENT );
				document.appendChild( comment );
				Element root = document.createElement( "ivysettings" );
				document.appendChild( root );
				Element resolvers = document.createElement( "resolvers" );
				root.appendChild( resolvers );
				element = document.createElement( type );
				resolvers.appendChild( element );
			}
		}

		public void save() throws TransformerFactoryConfigurationError, TransformerException, IOException
		{
			XmlUtil.saveHumanReadable( document, ivyFile );
		}

		public Element element = null;

		private Document document = null;
	}
}

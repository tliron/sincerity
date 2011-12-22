package com.threecrickets.sincerity;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;

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

public class Repositories
{
	//
	// Constants
	//

	public static final String REPOSITORY_SECTION_SEPARATOR = ":";

	//
	// Construction
	//

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
	// Operations
	//

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
		if( url != null )
			resolver.setRoot( url );
		resolver.setChecksums( "none" );
		boolean added = addResolver( section, resolver, true );

		if( added )
		{
			try
			{
				Appender appender = new Appender( "ibiblio" );
				appender.element.setAttribute( "checksums", "none" );
				if( url != null )
					appender.element.setAttribute( "root", url );
				appender.element.setAttribute( "m2compatible", "true" );
				appender.element.setAttribute( "name", name );
				appender.save();
			}
			catch( Exception x )
			{
				throw new SincerityException( "Could not append to repositories configuration", x );
			}
		}

		return added;
	}

	public boolean addPyPi( String section, String name, String url ) throws SincerityException
	{
		name = section + REPOSITORY_SECTION_SEPARATOR + name;
		if( ivy.getSettings().getResolver( name ) != null )
			return false;

		PyPiResolver resolver = new PyPiResolver();
		resolver.setSettings( ivy.getSettings() );
		resolver.setEventManager( ivy.getEventManager() );
		resolver.setName( name );
		if( url != null )
			resolver.setRoot( url );
		resolver.setChecksums( "none" );
		boolean added = addResolver( section, resolver, true );

		if( added )
		{
			try
			{
				Appender appender = new Appender( "pypi" );
				appender.element.setAttribute( "checksums", "none" );
				if( url != null )
					appender.element.setAttribute( "root", url );
				appender.element.setAttribute( "name", name );
				appender.save();
			}
			catch( Exception x )
			{
				throw new SincerityException( "Could not append to repositories configuration", x );
			}
		}

		return added;
	}

	public boolean remove( String section, String name )
	{
		name = section + REPOSITORY_SECTION_SEPARATOR + name;
		if( ivy.getSettings().getResolver( name ) == null )
			return false;

		// TODO

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final File ivyFile;

	private final Ivy ivy;

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

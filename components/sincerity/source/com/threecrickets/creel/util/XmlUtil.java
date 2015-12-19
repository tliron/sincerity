/**
 * Copyright 2015-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.creel.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML utilities.
 * 
 * @author Tal Liron
 */
public abstract class XmlUtil
{
	//
	// Static operations
	//

	/**
	 * Parses an XML document.
	 * 
	 * @param xml
	 *        The XML document
	 * @return The parsed document
	 * @throws ParserConfigurationException
	 *         In case of a SAX configuration error
	 * @throws SAXException
	 *         In case of an XML parsing error
	 * @throws IOException
	 *         In case of an I/O error
	 */
	public static Document parse( String xml ) throws ParserConfigurationException, SAXException, IOException
	{
		InputSource source = new InputSource( new StringReader( xml ) );
		// Note: builders are *not* thread-safe!
		DocumentBuilder builder = BUILDER_FACTORY.newDocumentBuilder();
		return builder.parse( source );
	}

	/**
	 * Gets the document element only if it matches the specific tag.
	 * 
	 * @param document
	 *        The document
	 * @param tag
	 *        The tag
	 * @return The document element or null
	 */
	public static Element getElement( Document document, String tag )
	{
		Element element = document.getDocumentElement();
		return ( ( element != null ) && tag.equals( element.getTagName() ) ) ? element : null;
	}

	/**
	 * Gets the child elements.
	 * 
	 * @param element
	 *        The element
	 * @return The matching child elements
	 */
	public static Collection<Element> getChildElements( Element element )
	{
		return getChildElements( element, null );
	}

	/**
	 * Gets the child elements only if they match the specific tag.
	 * 
	 * @param element
	 *        The element
	 * @param tag
	 *        The tag or null to match all child elements
	 * @return The matching child elements
	 */
	public static Collection<Element> getChildElements( Element element, String tag )
	{
		Collection<Element> elements = new ArrayList<Element>();
		NodeList children = element.getChildNodes();
		for( int i = 0, length = children.getLength(); i < length; i++ )
		{
			Node child = children.item( i );
			if( child instanceof Element )
			{
				Element childElement = (Element) child;
				if( ( tag == null ) || tag.equals( childElement.getTagName() ) )
					elements.add( childElement );
			}
		}
		return Collections.unmodifiableCollection( elements );
	}

	/**
	 * Gets the first child element with a specific tag.
	 * 
	 * @param element
	 *        The parent element
	 * @param tag
	 *        The child tag
	 * @return The child element or null if not found
	 */
	public static Element getFirstElement( Element element, String tag )
	{
		NodeList children = element.getChildNodes();
		for( int i = 0, length = children.getLength(); i < length; i++ )
		{
			Node child = children.item( i );
			if( child instanceof Element )
			{
				Element childElement = (Element) child;
				if( ( tag == null ) || tag.equals( childElement.getTagName() ) )
					return childElement;
			}
		}
		return null;
	}

	/**
	 * Gets the text of the first child element with a specific tag.
	 * 
	 * @param element
	 *        The parent element
	 * @param tag
	 *        The child tag
	 * @return The text of the first child element or null if not found
	 */
	public static String getFirstElementText( Element element, String tag )
	{
		return getFirstElementText( element, tag, null );
	}

	/**
	 * Gets the text of the first child element with a specific tag.
	 * 
	 * @param element
	 *        The parent element
	 * @param tag
	 *        The child tag
	 * @param defaultValue
	 *        The default value
	 * @return The text of the first child element or the default value if not
	 *         found
	 */
	public static String getFirstElementText( Element element, String tag, String defaultValue )
	{
		Element child = getFirstElement( element, tag );
		return child != null ? child.getTextContent() : defaultValue;
	}

	/**
	 * Iterable wrapper around child elements.
	 */
	public static class Elements implements Iterable<Element>
	{
		public Elements( Element element )
		{
			this( element, null );
		}

		public Elements( Element element, String tag )
		{
			elements = element != null ? getChildElements( element, tag ) : null;
		}

		@SuppressWarnings("unchecked")
		public Iterator<Element> iterator()
		{
			if( elements == null )
				return Collections.EMPTY_LIST.iterator();
			return elements.iterator();
		}

		private final Collection<Element> elements;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	public static final DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

	private XmlUtil()
	{
	}
}

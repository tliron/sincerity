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

package com.threecrickets.sincerity.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * XML utilities.
 * 
 * @author Tal Liron
 */
public abstract class XmlUtil
{
	public static final String COMMENT = " This configuration file was generated by Sincerity. ";

	public static final String COMMENT_FULL = "<!--" + COMMENT + "-->\n";

	public static void saveHumanReadable( Document document, File file ) throws TransformerFactoryConfigurationError, TransformerException, IOException
	{
		// Various indentation and UTF8 encoding bugs are worked around here
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setAttribute( "indent-number", new Integer( 4 ) );
		Transformer transformer = factory.newTransformer();
		transformer.setOutputProperty( OutputKeys.ENCODING, "utf-8" );
		transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
		transformer.setOutputProperty( "{http://xml.apache.org/xslt}indent-amount", "4" );
		OutputStreamWriter writer = new OutputStreamWriter( new FileOutputStream( file ), "UTF8" );
		transformer.transform( new DOMSource( document ), new StreamResult( writer ) );
		writer.close();
	}

	public static void removeTextNodes( Element element )
	{
		Node nextNode = element.getFirstChild();
		for( Node child = element.getFirstChild(); nextNode != null; )
		{
			child = nextNode;
			nextNode = child.getNextSibling();
			if( child.getNodeType() == Node.TEXT_NODE )
				element.removeChild( child );
			else if( child.getNodeType() == Node.ELEMENT_NODE )
				removeTextNodes( (Element) child );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private XmlUtil()
	{
	}
}

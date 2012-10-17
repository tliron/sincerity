//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2012 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.executeOnce('/sincerity/classes/')
document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/objects/')

var Sincerity = Sincerity || {}

/**
 * JavaScript-friendly wrapper over the JVM's XML DOM parsing and rendering services.
 * <p>
 * Additionally allows for building XML documents based on simple JSON-based DSL.
 * The JSON structure can be stored in MongoDB.
 * <p>
 * Note: This library modifies the String prototype.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.XML = Sincerity.XML || function() {
	/** @exports Public as Sincerity.XML */
    var Public = {}

	/**
	 * Converts an XML representation into a hierarchy of nodes.
	 * 
	 * @param {String} xml The XML representation 
	 * @returns {Sincerity.XML.Node}
	 */
	Public.from = function(xml) {
		var source = new org.xml.sax.InputSource(new java.io.StringReader(xml))
		var document = builder.parse(source)
		return new Public.Node(document)
	}
	
	/**
	 * Recursively converts a JavaScript value into an XML representation.
	 * 
	 * @param value The value
	 * @returns {String} The XML representation of value
	 */
	Public.to = function(value) {
		var xml = new java.io.StringWriter()
		var writer = outputFactory.createXMLStreamWriter(xml)
		
		writer.writeStartDocument()
		doWrite(value, writer)
		writer.writeEndDocument()
		writer.close()
		
		return String(xml.toString())
	}
	
	/**
	 * Makes an XML representation human readable by making it multi-line and indented.
	 * 
	 * @param {String} xml The XML representation
	 * @param {Number} [indent=2] The number of spaces per indent
	 * @returns {String} A human-readable XML representation
	 */
	Public.humanize = function(xml, indent) {
		indent = indent || 2
		
		var source = new javax.xml.transform.stream.StreamSource(new java.io.StringReader(xml))
		var result = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter())

		var transformer = transformerFactory.newTransformer()
		transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, 'yes')
		transformer.setOutputProperty('{http://xml.apache.org/xslt}indent-amount', indent) // Known bug! Setting this is absolutely necessary
		transformer.transform(source, result)
		
		return String(result.writer.toString())
	}

	/**
	 * Escapes &lt; and &gt; characters by representing them as XML entities.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.escapeElements = function(string) {
		return Sincerity.Objects.exists(string) ? String(string).replace(/</g, '&lt;').replace(/>/g, '&gt;') : ''
	}

	/**
	 * Escapes double quotes by representing them as XML entities.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.escapeDoubleQuotes = function(string) {
		return Sincerity.Objects.exists(string) ? String(string).replace(/\"/g, '&quot;') : ''
	}

	/**
	 * Escapes &lt;, &gt;, ', ", and & characters by representing them as XML entities.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.escapeText = function(string) {
		return Sincerity.Objects.exists(string) ? String(string).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;').replace(/'/g, '&apos;') : ''
	}

	/**
	 * Builds an XML element and its children.
	 * 
	 * @param params Any params that do not begin with an underscore are applied as
	 *        attributes to the element
	 * @param {String} params._tag The tag
	 * @param {Boolean} [params._value] True if params._key and params._cast should
	 *                  be applied to the params.value attribute instead of to params._content
	 * @param {String} [params._content] The element content
	 * @param {Array} [params._cast] If present, applied as arguments to calling {@link Templates.cast}
	 *                on params._content (or params.value)
	 * @param {String} [params._key] If present, fetches params._content (or params.value)
	 *                 from the params._textPack with this key
	 * @param {String[]} [params._classes] Turns into a params.class attribute
	 * @param {Sincerity.Internationalization.Pack} [params._textPack] The text pack to use for params._key
	 * @param {Object|Array} [params._children] One or more children elements (as params)  
	 * @param {Boolean} [params._human] True if to build the element as human-readable,
	 *        multi-line and indented
	 * @param {Boolean} [params._html] True if to use HTML mode (which is not XHTML)
	 * @returns {String} The element
	 */
	Public.build = function(params) {
		params = params ? Sincerity.Objects.clone(params) : {}

		if (params._merge) {
			var merge = Sincerity.Objects.array(params._merge)
			for (var m in merge) {
				Sincerity.Objects.merge(params, merge[m])
			}
		}

		params._human = params._human ? Number(params._human) : null
		params._children = params._children ? Sincerity.Objects.array(params._children) : null
				
		var humanize = Sincerity.Objects.exists(params._human)

		if (params._key && params._textPack) {
			if (params._value) {
				params.value = params._textPack.get(params._key)
			}
			else {
				params._content = params._textPack.get(params._key)
			}
		}
		
		if (params._cast) {
			if (params._value && params.value) {
				params.value = params.value.cast(params._cast)
			}
			else if (params._content) {
				params._content = params._content.cast(params._cast)
			}
		}

		if (params._classes) {
			params['class'] = Sincerity.Objects.array(params._classes).join(' ')
		}
		
		var indent = '\t'.repeat(params._human)
		
		var output = indent + '<' + params._tag
		for (var p in params) {
			if (p[0] != '_') {
				var param = params[p]
				if (Sincerity.Objects.exists(param)) {
					output += ' ' + p + '="' + Public.escapeDoubleQuotes(param) + '"'
				}
			}
		}
		
		if (params._children && params._children.length) {
			output += '>'
			if (humanize) {
				output += '\n'
			}
			
			for (var c in params._children) {
				var child = Sincerity.Objects.clone(params._children[c])
				if (params._html) {
					child._html = params._html
				}
				if (params._textPack) {
					child._textPack = params._textPack
				}
				if (humanize) {
					child._human = params._human + 1
				}
				output += Public.build(child)
			}
			
			output += indent + '</' + params._tag + '>'
		}
		else if (params._content) {
			if (params._html && humanize) {
				output += '>\n' + indent + '\t' + Public.escapeElements(params._content) + '\n' + indent + '</' + params._tag + '>'
			}
			else {
				output += '>' + Public.escapeElements(params._content) + '</' + params._tag + '>'
			}
		}
		else {
			if (params._html) {
				switch (String(params._tag)) {
					case 'input':
					case 'img':
						output += ' />'
						break
					default:
						output += '></' + params._tag + '>'
				}
			}
			else {
				output += '/>'
			}
		}

		if (humanize) {
			output += '\n'
		}

		return output
	}

	/**
	 * Represents an XML node (JavaScript wrapper over the JVM org.w3c.dom.Node).
	 * 
	 * @class
	 * @name Sincerity.XML.Node
	 * 
	 * @param {org.w3c.dom.Node} node The JVM node
	 * @param {Sincerity.XML.Node} [parent] The node's parent
	 * 
	 * @see Sincerity.XML#from
	 */
	Public.Node = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.XML.Node */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(node, parent) {
	    	this.node = node
	    	this.parent = parent
	    }

	    /**
		 * The parent node, or null if we are the root node.
		 * 
		 * @returns {Sincerity.XML.Node}
		 */
		Public.getParent = function() {
			return this.parent
		}
		
		/**
		 * The node name.
		 * 
		 * @returns {String}
		 */
		Public.getName = function() {
			return String(this.node.nodeName)
		}
		
		/**
		 * The node value.
		 * 
		 * @returns {String}
		 */
		Public.getValue = function() {
			return String(this.node.nodeValue)
		}
		
		/**
		 * The node type. Supported types:
		 * <ul>
		 * <li>'attribute'</li>
		 * <li>'cdata'</li>
		 * <li>'comment'</li>
		 * <li>'fragment'</li>
		 * <li>'document'</li>
		 * <li>'type'</li>
		 * <li>'element'</li>
		 * <li>'entity'</li>
		 * <li>'reference'</li>
		 * <li>'notation'</li>
		 * <li>'instruction'</li>
		 * <li>'text'</li>
		 * </ul>
		 * 
		 * @returns {String}
		 */
		Public.getType = function() {
			return getNodeType(this.node.nodeType)
		}

		/**
		 * The node prefix.
		 * 
		 * @returns {String}
		 */
		Public.getPrefix = function() {
			return String(this.node.prefix)
		}

		/**
		 * Gathers all attribute child nodes.
		 * When multiple attributes with the same name exist, the last one will be used.
		 * 
		 * @returns {Object}
		 */
		Public.getAttributes = function() {
			var r = {}

			var attributes = this.node.attributes
			if (attributes) {
				for (var c = 0, length = attributes.length; c < length; c++) {
					var attribute = new Sincerity.XML.Node(attributes.item(c))
					r[attribute.getName()] = attribute.getValue()
				}
			}
			
			return r
		}
		
		/**
		 * Gathers all child nodes, optionally by name and/or type.
		 * 
		 * @param {String} [name]
		 * @param {String} [type] See {@link Sincerity.XML.Node#getType}
		 * @returns {Sincerity.XML.Node[]}
		 */
		Public.getChildren = function(name, type) {
			var children = []

			var childNodes = this.node.childNodes
			for (var c = 0, length = childNodes.length; c < length; c++) {
				var childNode = new Sincerity.XML.Node(childNodes.item(c), this)
				if (name) {
					if (type) {
						if ((name == childNode.getName()) && (type == childNode.getType())) {
							children.push(childNode)
						}
					}
					else {
						if (name == childNode.getName()) {
							children.push(childNode)
						}
					}
				}
				else if (type) {
					if (type == childNode.getType()) {
						children.push(childNode)
					}
				}
				else {
					children.push(childNode)
				}
			}
			
			return children
		}

		/**
		 * Shortcut to gather all child nodes of type 'element'.
		 * 
		 * @param {String} [name]
		 * @returns {Sincerity.XML.Node[]}
		 * @see #getChildren 
		 */
		Public.getElements = function(name) {
			return this.getChildren(name, 'element')
		}
		
		/**
		 * Deeply gathers nodes of type 'element'.
		 * Arguments are names to be pursued in order of depth.
		 * 
		 * @returns {Sincerity.XML.Node[]}
		 */
		Public.gatherElements = function(/* arguments */) {
			var elements = []

			var names = []
			for (var a = 0, length = arguments.length; a < length; a++) {
				names.push(arguments[a])
			}

			doGatherElements(this, names, elements)

			return elements
		}
		
		/**
		 * Gathers and concatenates all child nodes of type 'text'.
		 * 
		 * @returns {String}
		 * @see #getChildren
		 */
		Public.getText = function() {
			var text = ''

			var texts = this.getChildren(null, 'text')
			for (var t in texts) {
				text += texts[t].getValue()
			}

			return text
		}

		/**
		 * Transforms the node and its children into an XML document, optionally
		 * making it human-readable in multiline, indented format.
		 * 
		 * @param {Number} [indent=0] If greater than 0, the number of spaces per indent in human-readable multiline format
		 * @returns {String} An XML representation
		 */
		Public.toXml = function(indent) {
			indent = indent || 0
			
			var source = new javax.xml.transform.dom.DOMSource(this.node)
			var result = new javax.xml.transform.stream.StreamResult(new java.io.StringWriter())

			var transformer = transformerFactory.newTransformer()
			if (indent > 0) {
				transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, 'yes')
				transformer.setOutputProperty('{http://xml.apache.org/xslt}indent-amount', indent) // Known bug! Setting this is absolutely necessary
			}
			transformer.transform(source, result)
			
			return String(result.writer.toString())
		}

		return Public
	}())
	
	//
	// Private
	//

	function doGatherElements(node, names, elements) {
		var firstName = names[0]
		var remainingNames = names.slice(1)
		var gathered = node.getElements(firstName)

		if (remainingNames.length == 0) {
			for (var g in gathered) {
				elements.push(gathered[g])
			}
		}
		else {
			for (var g in gathered) {
				doGatherElements(gathered[g], remainingNames, elements)
			}
		}
	}
	
	function doWriteArray(value, writer, parent) {
		for (var v in value) {
			if (parent) {
				writer.writeStartElement(parent)
			}
			doWrite(value[v], writer)
			if (parent) {
				writer.writeEndElement()
			}
		}
	}
	
	function doWrite(value, writer) {
		if (Sincerity.Objects.isObject(value)) {
			if (Sincerity.Objects.isDate(value)) {
				writer.writeCharacters(value.getTime())
				return
			}
			else if (Sincerity.Objects.isArray(value)) {
				doWriteArray(value, writer)
				return
			}
			else if (Sincerity.Objects.isDict(value, true)) {
				for (var v in value) {
					if (Sincerity.Objects.isArray(value[v])) {
						doWriteArray(value[v], writer, v)
					}
					else {
						writer.writeStartElement(v)
						doWrite(value[v], writer)
						writer.writeEndElement()
					}
				}
				return
			}
		}

		writer.writeCharacters(String(value))
	}
	
	function getNodeType(type) {
		switch (type) {
			case org.w3c.dom.Node.ATTRIBUTE_NODE:
				return 'attribute'
			case org.w3c.dom.Node.CDATA_SECTION_NODE:
				return 'cdata'
			case org.w3c.dom.Node.COMMENT_NODE:
				return 'comment'
			case org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE:
				return 'fragment'
			case org.w3c.dom.Node.DOCUMENT_NODE:
				return 'document'
			case org.w3c.dom.Node.DOCUMENT_TYPE_NODE:
				return 'type'
			case org.w3c.dom.Node.ELEMENT_NODE:
				return 'element'
			case org.w3c.dom.Node.ENTITY_NODE:
				return 'entity'
			case org.w3c.dom.Node.ENTITY_REFERENCE_NODE:
				return 'reference'
			case org.w3c.dom.Node.NOTATION_NODE:
				return 'notation'
			case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
				return 'instruction'
			case org.w3c.dom.Node.TEXT_NODE:
				return 'text'
			default:
				return ''
		}
	}
	
	//
	// Initialization
	//

	var builderFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
	var builder = builderFactory.newDocumentBuilder()
	var outputFactory = javax.xml.stream.XMLOutputFactory.newInstance()
	var transformerFactory = javax.xml.transform.TransformerFactory.newInstance()
	
	return Public
}()

/**
 * Escapes &lt; and &gt; characters by representing them as XML entities.
 * 
 * @methodOf String#
 * @returns {String}
 * @see Sincerity.XML#escapeElements
 */ 
String.prototype.escapeElements = String.prototype.escapeElements || function() {
	return Sincerity.XML.escapeElements(this)
}

/**
 * Escapes &lt;, &gt;, ', ", and & characters by representing them as XML entities.
 * 
 * @methodOf String#
 * @returns {String}
 * @see Sincerity.XML#escapeText
 */ 
String.prototype.escapeText = String.prototype.escapeText || function() {
	return Sincerity.XML.escapeText(this)
}

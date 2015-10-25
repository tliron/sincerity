//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2015 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.require(
	'/sincerity/jvm/')

var Sincerity = Sincerity || {}

/**
 * High-performance I/O operations.
 *  
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.IO = Sincerity.IO || function() {
	/** @exports Public as Sincerity.IO */
    var Public = {}
    
    /**
     * Copies bytes from one JVM channel to another, using an in-memory buffer.
     * 
     * @param {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/nio/channels/ReadableByteChannel.html">java.nio.channels.ReadableByteChannel</a>} inChannel
     * @param {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/nio/channels/WritableByteChannel">WritableByteChannel</a>} outChannel
     */
    Public.copyChannel = function(inChannel, outChannel) {
		var buffer = java.nio.ByteBuffer.allocate(bufferSize)
		while (inChannel.read(buffer) != -1) {
			buffer.flip()
			while (buffer.hasRemaining()) {
				outChannel.write(buffer)
			}
			buffer.clear()
		}
    }

    /**
     * Loads all data from a URI into a bytes array.
     * 
     * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/net/URI.html">java.net.URI</a>} uri
	 * @returns {byte[]}
     */
    Public.loadBytes = function(uri) {
    	uri = Sincerity.Objects.isString(uri) ? new java.net.URI(uri) : uri
		var fromChannel = java.nio.channels.Channels.newChannel(uri.toURL().openStream())
		try {
			var buffer = new java.io.ByteArrayOutputStream(bufferSize)
			var toChannel = java.nio.channels.Channels.newChannel(buffer)
			Public.copyChannel(fromChannel, toChannel)
			return buffer.toByteArray()
		}
		finally {
			fromChannel.close()
		}
    }

    /**
     * Loads all data from a URI as text.
	 * <p>
	 * Note that it does not return a string, but a buffer (which can be cast to a JavaScript
	 * String if required).
     * 
     * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/net/URI.html">java.net.URI</a>} uri
	 * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/nio/charset/Charset.html">java.nio.charset.Charset</a>} [charset=default encoding (most likely UTF-8)] The charset in which the file is encoded
	 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/nio/CharBuffer.html">java.nio.CharBuffer</a>}
     */
	Public.loadText = function(uri, charset) {
		charset = Sincerity.Objects.isString(charset) ? Sincerity.JVM.getCharset(charset) : (Sincerity.Objects.exists(charset) ? charset : Sincerity.JVM.getCharset())
		var bytes = Public.loadBytes(uri)
		return charset.decode(java.nio.ByteBuffer.wrap(bytes))
	}
	
	var bufferSize = 16 * 1024
    
	return Public
}()

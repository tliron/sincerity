//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2017 Three Crickets LLC.
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
     * Converts to URI instances if necessary.
     *
     * @param uri
     * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/net/URI.html">java.net.URI</a>}
     */
    Public.asUri = function(uri) {
    	return Sincerity.JVM.instanceOf(uri, java.net.URI) ? uri : new java.net.URI(String(uri))
    }

    /**
     * Converts to file instances if necessary.
     *
     * @param file
	 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>}
     */
    Public.asFile = function(file) {
    	return Sincerity.JVM.instanceOf(file, java.io.File) ? file : new java.io.File(String(file))
    }

    /**
     * Copies bytes from one JVM channel to another, using an in-memory buffer.
     * 
     * @param {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/nio/channels/ReadableByteChannel.html">java.nio.channels.ReadableByteChannel</a>} inChannel
     * @param {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/nio/channels/WritableByteChannel">java.nio.channels.WritableByteChannel</a>} outChannel
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
     * Loads all data from a URI as a bytes array.
     * 
     * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/net/URI.html">java.net.URI</a>} uri
	 * @returns {byte[]}
     */
    Public.loadBytes = function(uri) {
    	uri = Public.asUri(uri)
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
    	uri = Public.asUri(uri)
		charset = Sincerity.JVM.asCharset(charset)
		var bytes = Public.loadBytes(uri)
		return Sincerity.JVM.fromBytes(bytes, charset)
	}
	
	/**
	 * Download all data from a URI to a file.
	 * 
     * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/net/URI.html">java.net.URI</a>} uri
	 * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>} file The file or its path
	 */
	Public.download = function(uri, file) {
    	uri = Public.asUri(uri)
		file = Public.asFile(file).canonicalFile
		com.threecrickets.sincerity.util.IoUtil.copy(uri.toURL(), file)
	}
	
	var bufferSize = 16 * 1024
    
	return Public
}()

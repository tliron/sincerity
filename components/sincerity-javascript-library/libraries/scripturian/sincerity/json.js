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

document.require(
		'/sincerity/objects/',
		'/sincerity/classes/',
		'/sincerity/iterators/',
		'/sincerity/files/')

var Sincerity = Sincerity || {}

/**
 * JSON encoding and decoding. Uses the high-performance JSON Rhino
 * library if available, otherwise falls back to a 100%-JavaScript
 * version.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */

Sincerity.JSON = Sincerity.JSON || com.threecrickets.jvm.json.JSON

if (Sincerity.Objects.isJVM(Sincerity.JSON)) {
	/**
	 * Streaming JSON array parser.
	 * <p>
	 * The constructor accepts either a ready-to-use reader, or will create
	 * an efficient one for a file.
	 * 
	 * @class
	 * @name Sincerity.Iterators.JsonArray
	 * @param params
	 * @param {String|java.io.File} [params.file] The file or its path (ignored if params.reader is used)
	 * @param {java.io.Reader} [params.reader] A reader
	 */
	Sincerity.Iterators.JsonArray = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Iterators.JsonArray */
		var Public = {}
		
	    /** @ignore */
		Public._inherit = Sincerity.Iterators.Iterator
		
		/** @ignore */
		Public._construct = function(params) {
			if (Sincerity.Objects.exists(params.reader)) {
				this.reader = reader
			}
			else {
				this.reader = Sincerity.Files.openForTextReading(params.file, params.gzip)
			}
			
			this.tokener = Sincerity.JSON.createTokener(this.reader)

			// Make sure it's an array
			var c = nextClean.call(this)
	    	if (c == '[') {
		    	// We're good!
	    		this.hasNextFlag = true
	    		this.next()
	    	}
	    	else {
	    		// Not an array
	    		this.hasNextFlag = false
	    	}
		}
		
		Public.hasNext = function() {
			return this.hasNextFlag
		}
		
		Public.next = function() {
			var value = this.nextValue
    		
			var c = nextClean.call(this)
	    	if (c == ']') {
		    	// We're done!
	    		this.hasNextFlag = false
	    	}
	    	else if (c == ',') {
	    		this.nextValue = this.tokener.nextValue()
	    	}
	    	else {
	    		this.tokener.back()
	    		this.nextValue = this.tokener.nextValue()
	    	}
			
    		return value
		}
		
		Public.close = function() {
			this.reader.close()
		}
		
		//
		// Private
		//
		
		function nextClean() {
			return java.lang.Character.toString(this.tokener.nextClean())
		}
		
		return Public
	}())
}
else {
	// Fallback to JSON JavaScript library if the com.threecrickets.json library isn't found
	
	document.require('/sincerity/internal/json2/')
	
	Sincerity.JSON = {}
	
	/**
	 * Recursively converts MongoDB's extended JSON notation to
	 * ObjectId, DBRef, Date, RegExp, java.lang.Long and byte array objects as necessary.
	 * 
	 * @param {Object|Array} json The data
	 * @returns {Object|Array}
	 */
	Sincerity.JSON.fromExtendedJSON = function(json) {
		if (Sincerity.Objects.isArray(json)) {
			for (var j = 0, length = json.length; j < length; j++) {
				json[j] = Sincerity.JSON.fromExtendedJSON(json[j])
			}
		}
		else if (Sincerity.Objects.isObject(json)) {
			if (json.$long !== undefined) {
				// Note: Rhino will not let us use java.lang.Long instances! It will
				// immediately convert them to JavaScript Number instances.
				
				// It would probably be best to plug into a BigDecimal library
				return Number(json.$long)
			}
			
			if (json.$date !== undefined) {
				// See note for $long
				var timestamp = json.$date.$long !== undefined ? json.$date.$long : json.$date;
				return new Date(Number(timestamp))
			}
			
			if (json.$oid !== undefined) {
				return json.$oid
			}
			
			if (json.$regex !== undefined) {
				return new RegExp(json.$regex, json.$options)
			}
			
			for (var k in json) {
				json[k] = Sincerity.JSON.fromExtendedJSON(json[k])
			}
		}
	}
	
	/**
	 * Recursively converts a JavaScript value to MongoDB's extended JSON notation.
	 * 
	 * @param value The extended-JSON-compatible value
	 * @param {Boolean} [human=false] True to generate human-readable, multi-line, indented JSON
	 * @param {Boolean} [javaScript=false] True to generate JavaScript source code where applicable (breaks JSON!)
	 * @returns {String} The JSON representation of value
	 */
	Sincerity.JSON.to = function(value, human, javaScript) {
		return JSON.stringify(value)
		
		// TODO: extended JSON? JavaScript mode?
	}
	
	/**
	 * Converts a JSON representation into a hierarchy of JavaScript objects, arrays and strings.
	 * 
	 * @param {String} json The JSON string
	 * @param {Boolean} [extendedJson=false] True to interpret MongoDB's extended JSON notation,
	 *        creating ObjectId, DBRef, Date, RegExp, java.lang.Long and byte array objects where noted
	 * @returns {Object|Array}
	 */
	Sincerity.JSON.from = function(json, extendedJson) {
		json = JSON.parse(json)
		if (extendedJson) {
			Sincerity.JSON.fromExtendedJSON(json)
		}
		return json
	}
}
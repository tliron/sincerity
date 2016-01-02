//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2016 Three Crickets LLC.
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
	'/sincerity/jvm/')

var Sincerity = Sincerity || {}

/**
 * A simple-yet-powerful string interpolator.
 * <p>
 * Note: This library modifies the String prototype.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Templates = Sincerity.Templates || function() {
	/** @exports Public as Sincerity.Templates */
    var Public = {}

	/**
	 * Cast a template, which may contain variables in curly brackets. Three modes for casting are supported:
	 * <ul>
	 * <li>Using a series arguments, which each fill numbered variables {0}, {1}, {2} and so on</li>
	 * <li>Using dict/map of filling values</li>
	 * <li>Using a filling function</li>
	 * </ul>
	 * Variables with no matching filling are left as is.
	 * <p>
	 * Examples:
	 * <pre>
	 *    'Created {0} animals of type {1}!'.cast(9, 'cat')
	 * 
	 *    'Created {count} animals of type {type}!'.cast(9, {count: 9, type: 'cat'})
	 * 
	 *    var values = {count: 9, type: 'cat'}
	 *    'Created {count} animals of type {type}!'.cast(9, function(original, key) {
	 *       return values[key]
	 *    })
	 * </pre>
	 * 
	 * @param {String} template The template to cast
	 * @param {Object|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/Map.html">java.util.Map</a>|Function} [filling] If a function, accepts arguments (original, key)
	 * @returns {String}
	 */
	Public.cast = function(template, filling/*, arguments */) {
		template = Sincerity.Objects.exists(template) ? String(template) : ''

		if (!Sincerity.Objects.exists(filling)) {
			return template
		}
		
		if (typeof filling == 'function') {
			return template.replace(templateRegExp, filling)
		}
		
		if (filling instanceof java.util.Map) {
			return template.replace(templateRegExp, function(original, key) {
				return Sincerity.Objects.ensure(filling.get(key), original)
			})
		}
		
		if (!Sincerity.Objects.isDict(filling, true)) {
			// Convert extra arguments to a dict
			var convertedFilling = {}
			for (var a = 1, length = arguments.length; a < length; a++) {
				convertedFilling[a - 1] = String(arguments[a])
			}
			filling = convertedFilling
		}
		
		return template.replace(templateRegExp, function(original, key) {
			return Sincerity.Objects.ensure(filling[key], original)
		})
	}
    
    /**
     * Creates a filling using values from the JVM properties and/or the
     * operating system environment.
     * 
     * @param {Boolean} addProperties Whether to add the JVM properties
     * @param {Boolean} addEnv Whether to add the operating system environment
     */
    Public.createSystemFilling = function(addProperties, addEnv) {
    	var filling = {}
    	if (addProperties) {
    		Sincerity.Objects.merge(filling, Sincerity.JVM.fromProperties(java.lang.System.properties))
    	}
    	if (addEnv) {
    		Sincerity.Objects.merge(filling, Sincerity.JVM.fromMap(java.lang.System.getenv()))
    	}
    	return filling
    }

	//
	// Initialization
	//
	
	var templateRegExp = /{([^{}]*)}/g
	
	return Public
}()

/**
 * Cast a template, which may contain variables in curly brackets, 
 * using arguments (which fill {0}, {1}, {2} and so on),
 * a map of filling values, or a filling fetcher/generator function.
 * Variables with no matching filling are left as is.
 * 
 * @methodOf String#
 * @returns {String}
 * @see Sincerity.Templates#cast
 */ 
String.prototype.cast = String.prototype.cast || function(/* arguments */) {
	var args = [this].concat(Sincerity.Objects.slice(arguments))
	return Sincerity.Templates.cast.apply(null, args)
}

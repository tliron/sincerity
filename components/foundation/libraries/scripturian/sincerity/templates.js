//
// This file is part of the Sincerity Foundation Library for JavaScript
//
// Copyright 2011 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.opensource.org/licenses/lgpl-3.0.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

//
// Templates: A simple-yet-powerful string interpolation implementation.
// Template fillings can be JavaScript objects, JVM maps, functions,
// or the serialized function arguments.
//
// Examples:
//   'Created {count} animals of type {type}.'.cast(9, {count: 9, type: 'cat'})
//
//   'Created {0} animals of type {1}.'.cast(9, 'cat')
//
//   var values = {count: 9, type: 'cat'}
//   'Created {count} animals of type {type}.'.cast(9, function(original, key) {
//      return values[key]
//   })
//
// Note: This library modifies the String prototype.
//
// Version 1.0
//

document.executeOnce('/sincerity/objects/')

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
	 * @param {String} pattern The pattern to cast
	 * @param {Object|java.util.Map|Function} [filling] If a function, accepts arguments (original, key)
	 * @returns {String}
	 */
	Public.cast = function(template, filling/*, arguments */) {
		template = template || ''

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

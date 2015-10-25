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

var Sincerity = Sincerity || {}

/**
 * Lots of utilities to work with JavaScript objects, arrays and
 * strings, built specifically for Rhino's mixed JavaScript/JVM environment.
 * <p>
 * Note: This library modifies the String prototype.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.2
 */
Sincerity.Objects = Sincerity.Objects || function() {
	/** @exports Public as Sincerity.Objects */
    var Public = {}

	//
	// Values
	//
	
	/**
	 * True if the value is defined and not null (will be true even if the value is '0' or 'false').
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
	Public.exists = function(value) {
		// Note the order: we need the value on the right side for Rhino not to complain about non-JS objects
		return (undefined !== value) && (null !== value)
	}

	/**
	 * Always returns a value: if 'value' does not exist, returns 'defaultValue' instead.
	 * 
	 * @param value The value
	 * @param defaultValue The default value
	 * @see #exists
	 */
	Public.ensure = function(value, defaultValue) {
		return Public.exists(value) ? value : defaultValue
	}
	
	/**
	 * Cloning of dicts, arrays, dates and regexps.
	 * 
	 * @param value The value
	 * @param {Boolean} [deep=true] True to allow recursive deep cloning
	 */
	Public.clone = function(value, deep) {
		if (!Public.isObject(value)) {
			return value
		}
	
		if (Public.isDate(value)) {
			var copy = new Date()
			copy.setTime(value.getTime())
			return copy
		}
		
		if (value instanceof RegExp) {
			return new RegExp(value)
		}
	
		deep = Public.ensure(deep, true)
		
		if (Public.isArray(value)) {
			var copy = []
			if (deep) {
				for (var i in value) {
					copy[i] = Public.clone(value[i], true)
				}
			}
			else {
				for (var i in value) {
					copy[i] = value[i]
				}
			}
			return copy
		}
		
		if (typeof value === 'function') {
			return value
		}
		
		// See http://stackoverflow.com/a/728694/849021
		var copy = {}
		if (deep) {
			for (var k in value) {
				if (value.hasOwnProperty(k)) {
					copy[k] = Public.clone(value[k], true)
				}
			}
		}
		else {
			for (var k in value) {
				if (value.hasOwnProperty(k)) {
					copy[k] = value[k]
				}
			}
		}
		
		return copy
	}

	/**
	 * Checks equality for all object types. For arrays and dicts it would
	 * be a deep equality check.
	 * 
	 * @param x
	 * @param y
	 * @returns {Boolean} True if the values are deeply equal
	 */
    Public.areEqual = function(x, y) {
		if (!Public.isObject(x)) {
			if (!Public.isObject(y)) {
				if (Public.isNumber(x) && Public.isNumber(y) && isNaN(x) && isNaN(y)) {
					// Because NaN === NaN is normally false
					return true
				}
				return x === y
			}
			else {
				return false
			}
		}
	
		if (Public.isDate(x)) {
			if (Public.isDate(y)) {
				return x.getTime() === y.getTime()
			}
			else {
				return false
			}
		}
		
		if (x instanceof RegExp) {
			if (y instanceof RegExp) {
				return (x.source === y.source) && (x.global === y.global) && (x.ignoreCase === y.ignoreCase) && (x.multiline === y.multiline)
			}
			else {
				return false
			}
		}
	
		if (Public.isArray(x)) {
			if (Public.isArray(y)) {
				if (x.length !== y.length) {
					return false
				}
				for (var i in x) {
					if (!Public.areEqual(x[i], y[i])) {
						return false
					}
				}
				return true
			}
			else {
				return false
			}
		}
		
		if (typeof x === 'function') {
			if (typeof y === 'function') {
				return x.toString() === y.toString()
			}
			else {
				return false
			}
		}
		
		if (x.prototype !== y.prototype) {
			return false
		}
		
		var keys = []
		for (var key in x) {
			if (x.hasOwnProperty(key)) {
				Public.pushUnique(keys, key)
			}
		}
		for (var key in y) {
			if (y.hasOwnProperty(key)) {
				Public.pushUnique(keys, key)
			}
		}
		
		for (var k in keys) {
			var key = keys[k]
			if (!Public.areEqual(x[key], y[key])) {
				return false
			}
		}
		
		return true
    }

	/**
	 * True if the value is a wrapped JVM object (as opposed to a JavaScript object).
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
    Public.isJVM = function(value) {
    	if (!Public.exists(value)) {
    		return false
    	}
    	try {
    		// Might throw an exception in Rhino in some cases  (if value is a package or class)
    		return value.class !== undefined // TODO: can't JS objects also define this?
    	}
    	catch (x) {
    		return true
    	}
    	//var type = Object.prototype.toString.call(value)
    	//return (type === '[object jdk.internal.dynalink.beans.StaticClass]') || (type === '[object JavaObject]')
    }
    
	//
	// Numbers
	//
	
	/**
	 * True if the value is a number or <i>can be converted to</i> a number.
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
	Public.isNumber = function(value) {
		if (!Public.exists(value)) {
			return false
		}
		if (typeof value === 'number') {
			return true
		}
		// In Nashorn, a string ending with 'd' is numerical!
		if (Public.endsWith(value, 'd')) {
			return false
		}
		return !isNaN(value - 0)
	}
	
	/**
	 * True if the value is an integer or <i>can be converted to</i> an integer.
	 * <p>
	 * (Note that strings that would normally be converted to floats might be able
	 * to convert successfully to integers, too, because the decimal point would tell
	 * the conversion to stop, but in this case we are returning false: we require
	 * the string to be <i>entirely</i> convertible to an integer.)
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
	Public.isInteger = function(value) {
		if (!Public.exists(value)) {
			return false
		}
		return value % 1 === 0
	}
	
	//
	// Names
	//
	
	/**
	 * Makes sure the first character of the string is in upper case.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.capitalize = function(string) {
		string = String(string)
		return string.length ? string[0].toUpperCase() + string.substring(1) : ''
	}
	
	/**
	 * Concatenates the arguments into a single camel-cased string (each argument is capitalized
	 * except for the first argument, which is all lower case)
	 * 
	 * @returns {String}
	 */
	Public.camelCase = function(/* arguments */) {
		var args = (arguments.length && Public.isArray(arguments[0])) ? arguments[0] : arguments

		var camel = null
		for (var a = 0, length = args.length; a < length; a++) {
			var arg = String(args[a])
			if (camel) {
				camel += Public.capitalize(arg)
			}
			else {
				camel = arg.toLowerCase()
			}
		}

		return camel
	}
	
	//
	// Strings
	//
	
	/**
	 * True if the value is a JavaScript string <i>or</i> a JVM string. Despite their
	 * differences, they are often interchangeable. If you absolutely require a JavaScript
	 * string you can always cast the value like so: String(value). 
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
	Public.isString = function(value) {
		return (value instanceof java.lang.String) || (typeof value === 'string')
	}
	
	/**
	 * Always returns a string: non-existing values become entry strings, and arrays are joined.
	 */
	Public.string = function(value) {
		if (Public.isArray(value)) {
			return value.join('')
		}
		else if (Public.exists(value)) {
			return String(value)
		}
		return ''
	}

	/**
	 * Removes whitespace from beginning and end of the string.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.trim = function(string) {
		return Public.exists(string) ? String(string).replace(trimRegExp, '') : ''
	}

	/**
	 * Escapes single quotes with a backslash.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.escapeSingleQuotes = function(string) {
		return Public.exists(string) ? String(string).replace(/\'/g, "\\\'") : ''
	}
	
	/**
	 * Escapes double quotes with a backslash.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.escapeDoubleQuotes = function(string) {
		return Public.exists(string) ? String(string).replace(/\"/g, "\\\"") : ''
	}

	/**
	 * Escapes newlines with a backslash.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.escapeNewlines = function(string) {
		return Public.exists(string) ? String(string).replace(/\n/g, "\\n") : ''
	}

	/**
	 * Escapes regular expression special characters.
	 * 
	 * @param string The string
	 * @returns {String}
	 */
	Public.escapeRegExp = function(string) {
		// See: http://stackoverflow.com/a/6969486/849021
		return Public.exists(string) ? String(string).replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&') : ''
	}

	/**
	 * True if the string starts with the prefix.
	 * 
	 * @param string The string
	 * @param prefix The prefix
	 * @returns {Boolean}
	 */
	Public.startsWith = function(string, prefix) {
		if (!Public.exists(string)) {
			return false
		}
		string = String(string)
		var prefixLength = prefix.length
		if (string.length < prefixLength) {
			return false
		}
		return string.substring(0, prefixLength) === prefix
	}

	/**
	 * True if the string ends with the postfix.
	 * 
	 * @param string The string
	 * @param postfix The postfix
	 * @returns {Boolean}
	 */
	Public.endsWith = function(string, postfix) {
		if (!Public.exists(string)) {
			return false
		}
		string = String(string)
		var stringLength = string.length
		var postfixLength = postfix.length
		if (stringLength < postfixLength) {
			return false
		}
		return string.substring(stringLength - postfixLength) === postfix 
	}
	
	/**
	 * Returns 0 if string are equal, 1 if a > b, and -1 if a < b.
	 * Useful for sorting arrays of strings.
	 * 
	 * @param {String} a The string on the left
	 * @param {String} b The string on the right
	 * @returns {Number}
	 */
	Public.compareStrings = function(a, b) {
		return ((a == b) ? 0 : ((a > b) ? 1 : -1))
	}
	
	/**
	 * Repeats the string. When 'times' is 0, returns an empty string. 
	 * 
	 * @param string The string
	 * @param {Number} [times=0] The number of repetitions
	 * @returns {String}
	 */
	Public.repeat = function(string, times) {
		if (!times) {
			return ''
		}
		var array = []
		array.length = times
		return array.join(String(string))
	}

	/**
	 * Matches a string against a simple pattern.
	 * <p>
	 * The pattern may contain any number of '*' or '?' wildcards.
	 * Escape '*' or '?' using a preceding '\'.
	 * <p>
	 * An empty pattern matches everything.
	 * 
	 * @param string The string
	 * @param [pattern] The pattern
	 * @returns {Boolean} true if the string matches the pattern
	 */
	Public.matchSimple = function(string, pattern) {
		if (!Public.exists(pattern)) {
			// Match everything
			return true
		}
		
		pattern = String(pattern)
		if (!pattern.length || (pattern === '*')) {
			// Match everything
			return true
		}
		
		string = String(string)

		function nextWildcard(from) {
			from = from || 0
			var wildcard = pattern.substring(from).search(/[\*|\?]/)
			if (wildcard !== -1) {
				wildcard += from
			}
			if ((wildcard > 0) && (pattern.charAt(wildcard - 1) === '\\')) {
				// Don't count escaped wildcards
				pattern = pattern.substring(0, wildcard - 1) + pattern.substring(wildcard)
				return nextWildcard(wildcard) // note: pattern length was decreased by 1
			}
			return wildcard
		}

		var wildcard = nextWildcard()
		if (wildcard === -1) {
			// Strict pattern
			return pattern === string
		}
		
		// Convert pattern to regular expression
		var regexp = '^'
		while (wildcard !== -1) {
			regexp += Public.escapeRegExp(pattern.substring(0, wildcard))
			regexp += '[\\s\\S]'
			if (pattern.charAt(wildcard) === '*') {
				regexp += '*'
			}
			
			pattern = pattern.substring(wildcard + 1)
			wildcard = nextWildcard()
		}
		regexp += Public.escapeRegExp(pattern)
		regexp += '$'
		regexp = new RegExp(regexp)
		
		return null !== string.match(regexp)
	}

	//
	// Objects (dicts, arrays, dates, functions)
	//
	
	/**
	 * True if the value is a JavaScript Object (JVM objects <i>don't</i> count,
	 * but JavaScript functions <i>do</i> count as objects, because they
	 * can have properties assigned to them).
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
	Public.isObject = function(value) {
		if (!Public.exists(value)) {
			return false
		}
		
		var type
		
		try {
			type = typeof value
		}
		catch (x) {
			// This is a JVM object
			return false
		}
		
		if (value.getClass !== undefined) {
			// This is (likely) a JVM object
			return false
		}
		
		return (type === 'object') || (type === 'function')
	}
	
	//
	// Dates
	//
	
	/**
	 * True if the value is a JavaScript Date.
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
	Public.isDate = function(value) {
		return Public.exists(value) ? Object.prototype.toString.call(value) === '[object Date]' : false
	}
	
	//
	// Dicts
	//
	
	/**
	 * True is the value is any JavaScript Object <i>other than an array</i>
	 * (JavaScript functions count as dicts because they can have properties assigned to them).
	 * 
	 * @param value The value
	 * @param {Boolean} [strict=false] True to not count functions, Dates and RegExp objects as dicts
	 * @returns {Boolean}
	 */
	Public.isDict = function(value, strict) {
		if (strict) {
			return Public.isObject(value) && !Public.isArray(value) && (typeof value !== 'function') && !(value instanceof Date) && !(value instanceof RegExp)
		}
		else {
			return Public.isObject(value) && !Public.isArray(value)
		}
	}
	
	/**
	 * True if the value is a dict with no properties, or if it is not a dict.
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
	Public.isEmpty = function(value) {
		if (Public.isDict(value, true)) {
			for (var k in value) {
				// Even one property will cause us to return false
				return false
			}
		}
		return true
	}
	
	/**
	 * Copies all properties from one dict to another, modifying 'to' in the process.
	 * 
	 * @param to The destination dict
	 * @param from The source dict
	 * @param {String[]} keys Keys (leave blank to merge all keys)
	 * @returns The modified 'to'
	 */
	Public.merge = function(to, from, keys) {
		if (Public.isDict(from)) {
			if (keys) {
				for (var k in keys) {
					var key = keys[k]
					var value = from[key]
					if (undefined !== value) {
						to[key] = value
					}
				}
			}
			else {
				for (var f in from) {
					var value = from[f]
					if (undefined !== value) {
						to[f] = value
					}
				}
			}
		}
		return to
	}
	
	/**
	 * Recursively deletes all null properties from a dict, modifying the value
	 * in the process.
	 * 
	 * @param value The value
	 * @returns The modified value
	 */
	Public.prune = function(value) {
		// Deletes all null properties recursively
		if (Public.isArray(value)) {
			for (var k in value) {
				Public.prune(value[k])
			}
		}
		else if (Public.isDict(value, true)) {
			for (var k in value) {
				var v = value[k]
				if (null === v) {
					delete value[k]
				}
				else {
					Public.prune(v)
				}
			}
		}
		return value
	}

	/**
	 * Creates a flattened version of a dict, where all sub-dicts become root
	 * properties with hierarchical keys, with 'separator' as the path
	 * separator.
	 * <p>
	 * Flattening happens recursively, but can be stopped at any branch by
	 * using the 'separator' as a dict key. Everything under that key will
	 * be stored as is under the parent flat path.
	 * <p>
	 * A dict can avoid flattening by setting the '_flatten' key to false.
	 * 
	 * @param value The value
	 * @param {String} [separator='.'] The path separator
	 * @returns A flattened dict
	 */
	Public.flatten = function(value, separator, baseKey, flat, nonFlat) {
		flat = flat || {}
		nonFlat = nonFlat || {}

		if (Public.isDict(value, true) && (value._flatten !== false)) {
			separator = separator || '.'

			for (var key in value) {
				var v = value[key]
				if (key == separator) {
					nonFlat[baseKey] = v
				}
				else {
					if (baseKey) {
						key = baseKey + separator + key
					}
					Public.flatten(v, separator, key, flat, nonFlat)
				}
			}
		}
		else if (baseKey) {
			flat[baseKey] = value
		}
		
		Public.merge(flat, nonFlat)
		return flat
	}
	
	/**
	 * Clones the dict, such that iterating the clone's keys would be
	 * in their natural order (alphabetic).
	 * <p>
	 * Note that JavaScript dicts have an undefined iteration order, so
	 * this function must return a JVM LinkedHashMap.
	 * 
	 * @param value The value
	 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/LinkedHashMap.html">java.util.LinkedHashMap</a>}
	 */
	Public.sortByKeys = function(value) {
		return com.threecrickets.sincerity.util.CollectionUtil.sortedMap(value)
	}

	//
	// Arrays
	//
	
	/**
	 * True if the value is a JavaScript Array.
	 * 
	 * @param value The value
	 * @returns {Boolean}
	 */
	Public.isArray = function(value) {
		return Public.exists(value) ? Object.prototype.toString.call(value) === '[object Array]' : false
	}
	
	/**
	 * <i>Always</i> returns an array: if value is not already an array, wraps it in array, and if it does
	 * not exist returns an empty array.
	 * 
	 * @param value The value
	 * @returns {Array}
	 */
	Public.array = function(value) {
		if (Public.isArray(value)) {
			return value
		}
		else if (Public.exists(value)) {
			return [value]
		}
		return []
	}
	
	/**
	 * A version of Array.slice that works on array-like objects, too, such as 'arguments'.
	 * 
	 * @param array The array (or array-like object)
	 * @param {Number} start The start index
	 * @param {Number} end The end index
	 * @returns {Array}
	 */
	Public.slice = function(array, start, end) {
		return end === undefined ? Array.prototype.slice.call(array, start) : Array.prototype.slice.call(array, start, end)
	}
	
	/**
	 * Pushes all items from array2 into array1. Note that, unlike concat, this
	 * changes array1!
	 * 
	 * @param {Array} array1 The array to push into
	 * @param {Array} array2 The array to push from
	 * @returns {Array} array1
	 */
	Public.pushAll = function(array1, array2) {
		for (var a in array2) {
			array1.push(array2[a])
		}
		return array1
	}
	
	/**
	 * Pushes an item only if it does not already exist in the array.
	 * 
	 * @param {Array} array The array
	 * @param {Function} [testFn] An optional equality test function, defaults
	 *        to strict equality
	 * @returns {Boolean} True if the item was added
	 */
	Public.pushUnique = function(array, item, testFn) {
		var exists = false
		for (var a in array) {
			if (testFn) {
				if (testFn(array[a], item)) {
					exists = true
					break
				}
			}
			else if (Public.areEqual(array[a], item)) {
				exists = true
				break
			}
		}
		
		if (!exists) {
			array.push(item)
			return true
		}
		
		return false
	}
	
	/**
	 * Concats two arrays, while making sure not to add items from the second array
	 * that already exist in the first array.
	 * 
	 * @param {Array} array1 The first array
	 * @param {Array} array2 The second array
	 * @param {Function} [testFn] An optional equality test function, defaults
	 *        to strict equality
	 * @returns {Array} The combined array
	 */
	Public.concatUnique = function(array1, array2, testFn) {
		var additions = []
		for (var a2 in array2) {
			var item2 = array2[a2]
			
			var exists = false
			for (var a1 in array1) {
				if (testFn) {
					if (testFn(array1[a1], item2)) {
						exists = true
						break
					}
				}
				else if (Public.areEqual(array1[a1], item2)) {
					exists = true
					break
				}
			}
			
			if (!exists) {
				additions.push(item2)
			}
		}
		return array1.concat(additions)
	}
	
	/**
	 * Removes items from an array.
	 * 
	 * @param {Array} array The array
	 * @param {Array} item The items
	 * @param {Function} [testFn] An optional equality test function, defaults
	 *        to strict equality
	 * @returns {Array} The items that were removed
	 */
	Public.removeItems = function(array, items, testFn) {
		var removals = []
		for (var i in items) {
			var item = items[i]

			var length = array.length
			for (var a = 0; a < length; a++) {
				var remove = false
				if (testFn) {
					remove = testFn(array[a], item)
				}
				else {
					remove = Public.areEqual(array[a], item)
				}
				if (remove) {
					array.splice(a, 1)
					length--
					removals.push(item)
				}
			}
		}
		return removals
	}		

	/**
	 * Removes items from an array.
	 * 
	 * @param {Array} array The array
	 * @param {Array} positions The positions to remove
	 * @returns {Array} The items that were removed
	 */
	Public.removePositions = function(array, positions) {
		var removals = []
		for (var p in positions) {
			var position = positions[p]
			removals.push(array[position])
			array.splice(position, 1)
			for (var pp in positions) {
				if (positions[pp] > position) {
					positions[pp]--
				}
			}
		}
		return removals
	}

	//
	// Initialization
	//

	var trimRegExp = /^\s+|\s+$/g
		
	return Public
}()

/**
 * Removes whitespace from beginning and end of the string.
 * 
 * @methodOf String#
 * @see Sincerity.Objects#trim
 * @returns {String}
 */ 
String.prototype.trim = String.prototype.trim || function() {
	return Sincerity.Objects.trim(this)
}

/**
 * Escapes single quotes with a backslash. 
 * 
 * @methodOf String#
 * @see Sincerity.Objects#escapeSingleQuotes
 */ 
String.prototype.escapeSingleQuotes = String.prototype.escapeSingleQuotes || function() {
	return Sincerity.Objects.escapeSingleQuotes(this)
}

/**
 * Escapes double quotes with a backslash. 
 * 
 * @methodOf String#
 * @see Sincerity.Objects#escapeDoubleQuotes
 */ 
String.prototype.escapeDoubleQuotes = String.prototype.escapeDoubleQuotes || function() {
	return Sincerity.Objects.escapeDoubleQuotes(this)
}

/**
 * Escapes newlines with a backslash. 
 * 
 * @methodOf String#
 * @see Sincerity.Objects#escapeNewlines
 */ 
String.prototype.escapeNewlines = String.prototype.escapeNewlines || function() {
	return Sincerity.Objects.escapeNewlines(this)
}

/**
 * Escapes regular expression special characters. 
 * 
 * @methodOf String#
 * @see Sincerity.Objects#escapeRegExp
 */ 
String.prototype.escapeRegExp = String.prototype.escapeRegExp || function() {
	return Sincerity.Objects.escapeRegExp(this)
}

/**
 * True if the string starts with the prefix.
 * 
 * @methodOf String#
 * @see Sincerity.Objects#startsWith
 * @param {String} prefix
 * @returns {Boolean}
 */ 
String.prototype.startsWith = String.prototype.startsWith || function(prefix) {
	return Sincerity.Objects.startsWith(this, prefix)
}

/**
 * True if the string ends with the postfix.
 * 
 * @methodOf String#
 * @see Sincerity.Objects#endsWith
 * @param {String} postfix
 * @returns {Boolean}
 */ 
String.prototype.endsWith = String.prototype.endsWith || function(postfix) {
	return Sincerity.Objects.endsWith(this, postfix)
}

/**
 * Returns 0 if string are equal, 1 if a > b, and -1 if a < b.
 * 
 * @methodOf String#
 * @see Sincerity.Objects#compareStrings
 * @param {String} b
 * @returns {Number}
 */ 
String.prototype.compare = String.prototype.compare || function(b) {
	return Sincerity.Objects.compare(this, b)
}

/**
 * Makes sure the first character of the string is in upper case.
 * 
 * @methodOf String#
 * @see Sincerity.Objects#capitalize
 * @returns {String}
 */ 
String.prototype.capitalize = String.prototype.capitalize || function() {
	return Sincerity.Objects.capitalize(this)
}

/**
 * Repeats the string. When 'times' is 0, returns an empty string.
 * 
 * @methodOf String#
 * @see Sincerity.Objects#repeat
 * @param {Number} times
 * @returns {String}
 */ 
String.prototype.repeat = String.prototype.repeat || function(times) {
	return Sincerity.Objects.repeat(this, times)
}

/**
 * Matches a string against a simple pattern.
 * <p>
 * The pattern may contain any number of '*' or '?' wildcards.
 * Escape '*' or '?' using a preceding '\'.
 * <p>
 * An empty pattern matches everything.
 *
 * @methodOf String#
 * @see Sincerity.Objects#matchSimple
 * @param [pattern] The pattern
 * @returns {Boolean} true if the string matches the pattern
 */
String.prototype.matchSimple = String.prototype.matchSimple || function(pattern) {
	return Sincerity.Objects.matchSimple(this, pattern)
}

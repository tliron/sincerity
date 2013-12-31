//
// This file is part of the Sincerity Foundation Library
//
// Copyright 2011-2014 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.require(
	'/sincerity/jvm/',
	'/sincerity/json/')

var Sincerity = Sincerity || {}

/**
 * Useful shortcuts to platform-specific services and utilities.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Platform = Sincerity.Platform || function() {
	/** @exports Public as Sincerity.Platform */
    var Public = {}
    
    /**
     * The platform name.
     * 
     * @returns {String}
     */
    Public.name = String(executable.context.adapter.attributes.get('name'))
    
    /**
     * True if Nashorn.
     * 
     * @returns {Boolean}
     */
    Public.isNashorn = Public.name === 'Nashorn'

    /**
     * True if Mozilla Rhino.
     * 
     * @returns {Boolean}
     */
    Public.isRhino = Public.name === 'Rhino'

	/**
	 * The platform stack trace for an exception.
	 * 
	 * @param {Number} [skip=0] How many stack trace entries to skip
	 * @returns {String}
	 */
	Public.getStackTrace = function(exception, skip) {
		if (!exception.rhinoException) {
			return ''
		}
		skip = skip || 0
		var stackTrace = exception.rhinoException.scriptStackTrace
		return stackTrace.split('\n').slice(skip).join('\n')
	}

	/**
	 * The current platform stack trace.
	 * 
	 * @param {Number} [skip=0] How many stack trace entries to skip
	 * @returns {String}
	 */
	Public.getCurrentStackTrace = function(skip) {
    	if (Public.isNashorn) {
    		return ''
    	}

    	// We'll remove at least the first line (it's this very location)
		skip = skip || 0
		skip = skip + 1
		var stackTrace = new org.mozilla.javascript.JavaScriptException(null, null, 0).scriptStackTrace
		return stackTrace.split('\n').slice(skip).join('\n')
	}
	
	/**
	 * An exception stack trace. Supports both platform and JVM exceptions.
	 * 
	 * @param {Exception} exception The exception
	 * @param {Number} [skip=0] How many stack trace entries to skip
	 * @returns An object with .message and .stackTrace properties, both strings
	 */
	Public.getExceptionDetails = function(exception, skip) {
    	if (Sincerity.Objects.exists(exception.javaException)) {
			return {
				message: String(exception.javaException),
				stackTrace: Sincerity.JVM.getStackTrace(exception.javaException)
			}
		}
		else if (Sincerity.Objects.exists(exception.rhinoException)) {
			return {
				message: String(exception.rhinoException),
				stackTrace: Public.getStackTrace(exception, skip)
			}
		}
		else {
			return {
				message: Sincerity.Objects.isObject(exception) ? Sincerity.JSON.to(exception) : String(exception),
				stackTrace: Public.getStackTrace(exception, skip)
			}
		}
	}
	
	/**
	 * Creates a synchronized version of a function.
	 * 
	 * @param {Function} fn The function
	 * @returns {Function} The synchronized function
	 */
	Public.synchronize = function(fn) {
    	if (Public.isNashorn) {
    		var lock = new java.util.concurrent.locks.ReentrantLock()
    		return function() {
    			lock.lock()
    			try {
    				return fn.apply(this, arguments)
    			}
    			finally {
    				lock.unlock()
    			}
    		}
    	}
		return new org.mozilla.javascript.Synchronizer(fn)
	}
	
	return Public
}()

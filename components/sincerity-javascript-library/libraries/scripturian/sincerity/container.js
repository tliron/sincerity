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

document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/files/')
document.executeOnce('/sincerity/objects/')

var Sincerity = Sincerity || {}

/**
 * Useful shortcuts to Sincerity-specific services and utilities.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Container = Sincerity.Container || function() {
	/** @exports Public as Sincerity.Container */
    var Public = {}
    
    /**
     * The container root path.
     * 
     * @returns {<a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/File.html">java.io.File</a>}
     */
    Public.root = com.threecrickets.bootstrap.Bootstrap.attributes.get('com.threecrickets.sincerity.containerRoot')

    /**
     * The current path for nested execution.
     * 
     * @returns {<a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/File.html">java.io.File</a>}
     */
    Public.here = null
    
    /**
     * Builds an absolute file relative to the current path.
     * <p>
     * The arguments are path segments.
     * 
     * @returns {<a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/File.html">java.io.File</a>}
     * @see Sincerity.Files#build
     */
    Public.getFileFromHere = function(/* arguments */) {
    	return Sincerity.Files.build(Public.here, Sincerity.Objects.slice(arguments))
    }

	/**
	 * Executes the document represented by the file, while keeping track of the current path.
	 * 
	 * @param {String|<a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/File.html">java.io.File</a>} file The path to execute, relative to the 'here' location
	 */
	Public.execute = function(file) {
    	if (!(file instanceof java.io.File)) {
    		file = Public.getFileFromHere(file)
    	}

		pushHere(file.directory ? file : file.parentFile)
    	document.execute('/' + sincerity.container.getRelativePath(file) + '/')
    	popHere()
	}

	/**
	 * Recursively executes all documents in a directory, while keeping track of the current path.
	 * 
	 * @param {String|<a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/io/File.html">java.io.File</a>} file The path to execute, relative to the 'here' location
	 * @param {String[]} [exclusions] Paths to avoid
	 */
	Public.executeAll = function(file, exclusions) {
    	if (!(file instanceof java.io.File)) {
    		file = Public.getFileFromHere(file)
    	}

    	if (!file.directory) {
    		return
    	}
    	
		var children = listFiles(file)
		for (var c in children) {
			var child = children[c]
			if (exclusions) {
				var excluded = false
				for (var e in exclusions) {
					if (child.name == exclusions[e]) {
						excluded = true
						break
					}
				}
				if (excluded) {
					continue
				}
			}
			Public.execute(child)
		}
	}
	
	/**
	 * If the JVM class does not exist, adds and installed dependencies.
	 * <p>
	 * The arguments after the first are each an array of values to send to a
	 * "dependencies:add" command.
	 * 
	 * @param {String} className The JVM class name
	 * @returns {<a href="http://docs.oracle.com/javase/1.5.0/docs/api/index.html?java/lang/Class.html">java.lang.Class</a>} The class or null if not found
	 */
	Public.ensureClass = function(className/*, dependencies*/) {
		var theClass = Sincerity.JVM.getClass(className)
		if (!Sincerity.Objects.exists(theClass)) {
			var length = arguments.length
			if (length > 1) {
				for (var d = 1; d < length; d++) {
					var runArguments = ['dependencies:add']
					for (var a in arguments[d]) {
						runArguments.push(arguments[d][a])
					}
					sincerity.run(runArguments)
				}
				sincerity.run(['artifacts:install'])
				return null
			}
		}
		return theClass
	}

	//
	// Private
	//
	
    var hereStack = []

	function pushHere(dir) {
		hereStack.push(Public.here)
		Public.here = dir
	}
	
	function popHere() {
		Public.here = hereStack.pop()
	}
	
	function removeExtension(filename) {
		var last = filename.lastIndexOf('.')
		if (last != -1) {
			return filename.substring(0, last)
		}
		return filename
	}
	
	function listFiles(dir, filesOnly) {
		var files = []
		var jvmArray = dir.listFiles()
		for (var i in jvmArray) {
			var file = jvmArray[i]
			if (file.hidden || (filesOnly && file.directory)) {
				continue
			}
			files.push(file)
		}
		files.sort(function(a, b) {
			var aName = removeExtension(a.name)
			var bName = removeExtension(b.name)
			return aName.compareTo(bName)
		})
		return files
	}

	return Public
}()

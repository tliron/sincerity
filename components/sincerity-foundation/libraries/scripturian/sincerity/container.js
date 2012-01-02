//
// This file is part of the Sincerity Foundation Library
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

    Public.getClass = function(name) {
    	return sincerity.container.dependencies.classLoader.loadClass(name)
    }

    Public.here = null
    
    Public.getFileFromHere = function(/* arguments */) {
    	importClass(java.io.File)

    	var file = Public.here
    	for (var i in arguments) {
    		var part = arguments[i]
    		if ('..' == part) {
    			file = file.parentFile
    		}
    		else if ('.' != part) {
    			file = new File(file, part).absoluteFile
    		}
    	}
    	
    	return file
    }

	/**
	 * Executes all documents represented by the file, while keeping track of the current TODO
	 * 
	 * @param {String|java.io.File} The path to execute, relative to the 'here' location.
	 */
	Public.execute = function(file) {
    	importClass(java.io.File)
    	
    	if (!(file instanceof File)) {
    		file = Public.getFileFromHere(file)
    	}

		pushHere(file.directory ? file : file.parentFile)
    	document.execute('/' + sincerity.container.getRelativePath(file))
    	popHere()
	}

	Public.executeAll = function(file) {
    	importClass(java.io.File)
    	
    	if (!(file instanceof File)) {
    		file = Public.getFileFromHere(file)
    	}

    	if (!file.directory) {
    		return
    	}
    	
		var children = listFiles(file)
		for (var c in children) {
			Public.execute(children[c])
		}
	}

	//
	// Private
	//
	
    var hereStack = []

	function pushHere (dir) {
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

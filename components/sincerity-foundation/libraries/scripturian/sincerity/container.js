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
 * Useful shortcuts to Savory-specific services and utilities.
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

	Public.pushHere = function(dir) {
		hereStack.push(Public.here)
		Public.here = dir
	}
	
	Public.popHere = function() {
		Public.here = hereStack.pop()
	}
    
	/**
	 * Executes all documents represented by the file, while keeping track of the current
	 * 
	 * @param {String|java.io.File} The path to execute, relative to the 'here' location.
	 */
	Public.include = function(file) {
    	importClass(java.io.File)
    	
    	if (!(file instanceof File)) {
    		file = Public.getFileFromHere(file)
    	}
    	
    	if (file.directory) {
			// Execute all non-hidden files and subdirectories in the directory
    		var children = file.listFiles()
    		for (var c in children) {
    			var child = children[c]
        		if (!child.hidden) {
        			if (child.directory) {
        				Public.pushHere(child)
        				document.execute('/' + sincerity.container.getRelativePath(child) + '/')
        				Public.popHere()
        			}
        			else {
        				Public.pushHere(file)
        				document.execute('/' + sincerity.container.getRelativePath(child))
        				Public.popHere()
        			}
        		}
    		}
    	}
    	else {
    		// Execute the first file with this name in the directory
    		var dir = file.parentFile
    		var name = file.name.split('\\.', 2)[0]
    		var children = dir.listFiles()
    		for (var c in children) {
    			var child = children[c]
    			var childName = child.name.split('\\.', 2)[0]
    			if (name == childName) {
        			Public.pushHere(dir)
    				document.execute('/' + sincerity.container.getRelativePath(child))
    				Public.popHere()
    				break
    			}
    		}
    	}
	}
	
    var hereStack = []
	
	return Public
}()

//
// This file is part of the Savory Foundation Library for JavaScript
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

var Savory = Savory || {}

/**
 * Useful shortcuts to Savory-specific services and utilities.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Savory.Sincerity = Savory.Sincerity || function() {
	/** @exports Public as Savory.Sincerity */
    var Public = {}
    
    Public.here = null

	/**
	 * Executes all 
	 * 
	 * @param {String|java.io.File} The path to execute, relative to the container's root
	 */
	Public.include = function(file) {
    	importClass(java.io.File)
    	
    	if (!(file instanceof File)) {
    		file = new File(Public.here, file)
    	}
    	if (file.directory) {
    		var files = file.listFiles()
    		for (var f in files) {
    			hereStack.push(Public.here)
    			Public.here = files[f]
    			document.execute('/' + sincerity.container.getRelativePath(Public.here))
    			Public.here = hereStack.pop()
    		}
    	}
    	else {
    		var name = file.name.split('\\.', 2)[0]
    		var files = file.parentFile.listFiles()
    		for (var f in files) {
    			var here = files[f]
    			var hereName = here.name.split('\\.', 2)[0]
    			if (name == hereName) {
        			hereStack.push(Public.here)
    				Public.here = here
    				document.execute('/' + sincerity.container.getRelativePath(Public.here))
        			Public.here = hereStack.pop()
    			}
    		}
    	}
	}
    
    Public.getClass = function(name) {
    	return sincerity.container.dependencies.classLoader.loadClass(name)
    }
    
    var hereStack = []
	
	return Public
}()

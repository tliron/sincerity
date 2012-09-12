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

document.executeOnce('/sincerity/files/')
document.executeOnce('/sincerity/objects/')

var Sincerity = Sincerity || {}

/**
 * "Annotations" are specially delimited markers in textual files.
 * They can be used to insert flags and properties for special processing of files.
 * This library helps you gather these annotations in an easy-to-process structure.
 * <p>
 * The default delimiter is designed to allow annotations to be inserted into
 * Scripturian block comments, in which the comment begins with an equal sign
 * and the annotation name is separated from its value by a space:
 * <pre>
 * &lt;%# = my.value.name This is the value %&gt;
 * </pre>
 * However, you can supply your own custom regular expression.
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Sincerity.Annotations = Sincerity.Annotations || function() {
	/** @exports Public as Sincerity.Annotations */
	var Public = {}
	
	//
	// Public
	//
	
	/**
	 * Gathers annotations from all files in a directory.
	 * 
	 * @param {java.io.File} dir The directory
	 * @param {RegExp} [re] Optional annotation regular expression override
	 * @returns {Object} A dict of filenames mapped to dicts of their annotations;
	 *           the filename extension is not included
	 */
	Public.getAll = function(dir, re) {
		var annotations = {}
		
		var files = dir.listFiles()
		for (var f in files) {
			var file = files[f]
			var name = Public.getNameFromFile(file)
			annotations[name] = Public.get(file, re)
		}
		
		return annotations
	}
	
	/**
	 * Gathers annotations from a textual file.
	 * 
	 * @param {java.io.File} file The file
	 * @param {RegExp} [re] Optional annotation regular expression override
	 * @returns {Object} A dict of annotation names mapped to their string values;
	 *           annotations with no values will be mapped to empty strings 
	 */
	Public.get = function(file, re) {
		var annotations = {}
		
		var text = Sincerity.Files.loadText(file)
		if (Sincerity.Objects.exists(text)) {
			re = re ? new RegExp(re) : new RegExp(defaultRE)
			var match = re.exec(text)
			while (null !== match) {
				var line = match[1]
				var space = line.indexOf(' ')
				if (space != -1) {
					annotations[line.substring(0, space)] = line.substring(space + 1)
				}
				else {
					annotations[line] = ''
				}
				var match = re.exec(text)
			}
		}
		
		return annotations
	}
	
	/**
	 * Gets the filename without its extension.
	 *  
	 * @param {java.io.File} file The file
	 * @returns {String} The filename without its extension
	 */
	Public.getNameFromFile = function(file) {
		var name = String(file.name)
		name = name.substring(0, name.indexOf('.'))
		return name
	}
	
	//
	// Private
	//
	
	var defaultRE = /\<%# = (.*) %\>/g

	return Public
}()


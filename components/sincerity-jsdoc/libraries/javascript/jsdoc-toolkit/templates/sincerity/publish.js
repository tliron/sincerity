//
// This file is part of the Sincerity JsDoc Template
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

//
// String Enhancements
//

var resolveLinksRegExp = /\{@link ([^} ]+) ?\}/gi

String.prototype.toLink = function(from) {
	var s = this.split('#')

	var namespace = s[0] || from

	var isArray = false
	if (namespace.substring(namespace.length - 2) == '[]') {
		// Array notation
		namespace = namespace.substring(0, namespace.length - 2)
		isArray = true
	}

	if (!getNode(namespace)) {
		// Unknown symbol
		return this.toHtml(from)
	}
	
	var name = (s[0] ? this : 'this.' + s[1]).replace(/#/g, '.')
	var item = s[1] ? namespace + '.' + s[1] : null

	return '<span class="open-namespace" namespace="' + encodeURIComponent(namespace) + '"' + (item ? ' item="' + encodeURIComponent(item) + '"' : '') + '>' + name + '</span>'
}

String.prototype.toLinks = function(from) {
	var r = []
	var links = this.split('|')
	for (var l in links) {
		r.push(links[l].toLink(from))
	}
	return r.join('|')
}

String.prototype.toHtml = function(from) {
	var str = this.replace(resolveLinksRegExp, function(match, symbolName) {
		return symbolName.toLink(from)
	})
	return str
}

//
// Symbol Enhancements
//
	
JSDOC.Symbol.prototype.parseParams = function() {
	var r = {signature: [], listed: []}
	for (var p in this.params) {
		var param = this.params[p]
		if (param.name.indexOf('.') == -1) {
			r.signature.push(param)
		}
		if (param.type || param.desc) {
			r.listed.push(param)
		}
	}
	return r
}

JSDOC.Symbol.prototype.getAllProperties = function() {
	var properties = []
	for (var p in this.properties) {
		var property = this.properties[p]
		if (!property.isNamespace && (property.isa != 'CONSTRUCTOR')) {
			properties.push(property)
		}
	}
	return properties
}

JSDOC.Symbol.prototype.getAllMethods = function() {
	if (this.isa == 'CONSTRUCTOR') {
		// Include constructor as a method
		return [this].concat(this.methods)
	}
	return this.methods
}

JSDOC.Symbol.prototype.toLink = function(from) {
	return this.nametoLink(from)
}

//
// Param Utils
//

function niceShortParam(param, includeDefault) {
	return (param.isOptional ? '<span class="optional">' : '') + param.name + (param.isOptional ? '</span>' : '')
}

function niceParam(param, includeDefault) {
	return (param.isOptional ? '<span class="optional">' : '') + param.name + (param.defaultValue ? '=' + param.defaultValue : '') + (param.isOptional ? '</span>' : '')
}

//
// Symbol Tree
//

var root = {children: []}

function getNode(fullName, create) {
	var split = fullName.lastIndexOf('.')
	var parentFullName = split == -1 ? null : fullName.substring(0, split)
	var shortName = split == -1 ? fullName : fullName.substring(split + 1)
	var parent = parentFullName ? getNode(parentFullName, create) : root
			
	JSDOC.opt.D.extscript = JSDOC.opt.D.extscript || 'scripts/ext-js/ext-all.js'
	JSDOC.opt.D.extstyle = JSDOC.opt.D.extstyle || 'style/ext-js/ext-theme-neptune/ext-theme-neptune-all.css'

	if (!parent) {
		return null
	}
	
	var child
	for (var c in parent.children) {
		child = parent.children[c]
		if (child.text == shortName) {
			return child
		}
	}
	
	if (!create) {
		return null
	}

	child = {
		id: fullName,
		text: shortName,
		children: []
	}
	parent.children.push(child)
	parent.expanded = true
	return child
}

function sortChildren(children) {
	children.sort(function(a, b) {
		return ((a.text == b.text) ? 0 : ((a.text > b.text) ? 1 : -1))
	})
	for (var c in children) {
		sortChildren(children[c].children)
	}
}

//
// Files
//

var uniqueFileNames = {}

function safeSourceFileName(fileName) {
	return fileName.replace(/\.\.?[\\\/]/g, '').replace(/[\\\/]/g, '_').replace(/\:/g, '_')
}

IO.copyRecursive = function(inPath, outPath) {
	var inPath = new File(inPath)
	var inPathLength = inPath.toString().length()
	var files = IO.ls(inPath, 10)
	for (var n = 0, len = files.length; n < len; n++) {
		var outFile = new File(outPath + files[n].substring(inPathLength))
		var outPathParts = outFile.toString().replace('\\', '/').split('/')
		outPathParts = outPathParts.splice(0, outPathParts.length-1)
		IO.mkPath(outPathParts)
		IO.copyFile(files[n], outPathParts.join(SYS.slash))
	}
}
	
//
// Publish!
//

function publish(symbolSet) {
	var baseDir = JSDOC.opt.t || (SYS.pwd + '..' + SYS.slash + 'templates' + SYS.slash + 'sincerity')
	var contentDir = baseDir + SYS.slash + 'content'
	var libDir = baseDir + SYS.slash + 'lib'
	var webDir = baseDir + SYS.slash + '..' + SYS.slash + '..' + SYS.slash + '..' + SYS.slash + '..' + SYS.slash + 'web'
	var extScriptsDir = webDir + SYS.slash + 'scripts' + SYS.slash + 'ext-js'
	var extStyleDir = webDir + SYS.slash + 'style' + SYS.slash + 'ext-js'

	var outputDir = JSDOC.opt.d || (SYS.pwd + '..' + SYS.slash + 'out' + SYS.slash + 'sincerity')
	var styleDir = outputDir + SYS.slash + 'style'
	var mediaDir = outputDir + SYS.slash + 'media'
	var scriptsDir = outputDir + SYS.slash + 'scripts'
	var dataDir = outputDir + SYS.slash + 'data'
	var namespaceDir = outputDir + SYS.slash + 'namespace'
	var sourceDir = outputDir + SYS.slash + 'source'
	
	// Load libraries

	load(libDir + SYS.slash + 'json2.js')
	var indexTemplate = new JSDOC.JsPlate(baseDir + SYS.slash + 'templates' + SYS.slash + 'index.html')
	var documentationTemplate = new JSDOC.JsPlate(baseDir + SYS.slash + 'templates' + SYS.slash + 'documentation.js')
	var namespaceTemplate = new JSDOC.JsPlate(baseDir + SYS.slash + 'templates' + SYS.slash + 'namespace.html')

	// Create directories
	
	IO.mkPath(outputDir)
	IO.mkPath(styleDir)
	IO.mkPath(mediaDir)
	IO.mkPath(scriptsDir)
	IO.mkPath(dataDir)
	IO.mkPath(namespaceDir)
	IO.mkPath(sourceDir)
	
	var symbols = symbolSet.toArray()
	
	// Create unique file names

	if (JSDOC.opt.u) {
		var counts = {}
		for (var s in symbols) {
			var symbol = symbols[s]
			if (symbol.isNamespace || (symbol.isa == 'CONSTRUCTOR')) {
				var lcAlias = symbol.alias.toLowerCase() // All the other templates seem to use lower case files in unique mode, so we will, too
				counts[lcAlias] = counts[lcAlias] ? counts[lcAlias] + 1 : 1
				uniqueFileNames[symbol.alias] = (counts[lcAlias] > 1) ? lcAlias + '_' + counts[lcAlias] : lcAlias
			}
		}
	}
	
	// Create symbol tree

	for (var s in symbols) {
		var symbol = symbols[s]
		if (JSDOC.opt.D.noGlobal && (symbol.name == '_global_')) {
			continue
		}
		if (symbol.isNamespace || (symbol.isa == 'CONSTRUCTOR')) {
			var node = getNode(symbol.alias, true)
			node.iconCls = symbol.isa == 'CONSTRUCTOR' ? 'class' : 'namespace'
		}
	}
	
	// Generate files for namespaces and classes
	
	for (var s in symbols) {
		var symbol = symbols[s]
		if (JSDOC.opt.D.noGlobal && (symbol.name == '_global_')) {
			continue
		}
		if (symbol.isNamespace || (symbol.isa == 'CONSTRUCTOR')) {
			var content = namespaceTemplate.process(symbol)
			var fileName = JSDOC.opt.u ? uniqueFileNames[symbol.alias] : symbol.alias
			IO.saveFile(namespaceDir, fileName + '.html', content)
		}
	}
	
	// Generate hilighted source files
	
	if (!JSDOC.opt.s) {
		for (var s in JSDOC.opt.srcFiles) {
			var srcFile = JSDOC.opt.srcFiles[s]
			var fileName = safeSourceFileName(srcFile)
			var source = {path: srcFile, name: fileName, charset: IO.encoding, hilited: ''}
			JSDOC.PluginManager.run('onPublishSrc', source)
			if (source.hilited) {
				IO.saveFile(sourceDir, fileName + '.html', source.hilited)
			}
		}
	}
	
	// Generate namespaces.json
	
	sortChildren(root.children)
	IO.saveFile(dataDir, 'namespaces.json', JSON.stringify(root.children, null, '\t'))

	// Base files
	
	IO.saveFile(outputDir, 'index.html', indexTemplate.process())
	IO.saveFile(scriptsDir, 'documentation.js', documentationTemplate.process())
	IO.copyFile(contentDir + SYS.slash + 'documentation.css', styleDir, 'documentation.css')
	IO.copyFile(contentDir + SYS.slash + 'brick.png', mediaDir, 'brick.png')
	IO.copyFile(contentDir + SYS.slash + 'cog.png', mediaDir, 'cog.png')
	
	if (IO.exists(extScriptsDir)) {
		IO.copyRecursive(extScriptsDir, scriptsDir + SYS.slash + 'ext-js')
	}
	if (IO.exists(extStyleDir)) {
		IO.copyRecursive(extStyleDir, styleDir + SYS.slash + 'ext-js')
	}
}

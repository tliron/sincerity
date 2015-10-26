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

document.require(
	'/sincerity/dependencies/',
	'/sincerity/classes/',
	'/sincerity/objects/',
	'/sincerity/io/',
	'/sincerity/xml/',
	'/sincerity/cryptography/')

var Sincerity = Sincerity || {}
Sincerity.Dependencies = Sincerity.Dependencies || {}

/**
 * Dependency management support for <a href="https://maven.apache.org/">Maven</a> m2 repositories (also known as "ibiblio").
 * <p>
 * Supports reading the repository URI structure, retrieving and parsing ".pom" and "maven-metadata.xml" data,
 * interpreting module identifiers (group/name/version), applying version ranges, downloading ".jar" files,
 * and validating against signatures in ".sha1" or ".md5" files.
 * <p>
 * For convenience, we also support the <a href="http://ant.apache.org/ivy/">Ivy</a>-style "+" version range, even though
 * it is not part of the Maven standard. 
 * 
 * @namespace
 * 
 * @author Tal Liron
 */
Sincerity.Dependencies.Maven = Sincerity.Dependencies.Maven || function() {
	/** @exports Public as Sincerity.Dependencies.Maven */
	var Public = {}

	/**
	 * @class
	 * @name Sincerity.Dependencies.Maven.ModuleIdentifier
	 */
	Public.ModuleIdentifier = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.Maven.ModuleIdentifier */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Sincerity.Dependencies.ModuleIdentifier
	    
	    /** @ignore */
	    Public._construct = function(group, name, version) {
	    	if (arguments.length == 1) {
	    		var parts = group.split(':')
	    		this.group = parts[0]
		    	this.name = parts[1]
		    	this.version = parts[2]
	    	}
	    	else {
		    	this.group = group
		    	this.name = name
		    	this.version = version
	    	}
	    	
	    	// After resolve
	    	this.uri = null
	    }

	    Public.isEqual = function(moduleIdentifier) {
	    	return (this.group == moduleIdentifier.group) && (this.name == moduleIdentifier.name) && (this.version == moduleIdentifier.version)
	    }
	    
	    Public.toString = function() {
	    	return 'maven:' + this.group + ':' + this.name + ':' + this.version
	    }
	
	    return Public
	}())

	/**
	 * @class
	 * @name Sincerity.Dependencies.Maven.ModuleConstraints
	 */
	Public.ModuleConstraints = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.Maven.ModuleConstraints */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Sincerity.Dependencies.ModuleConstraints
	    
	    /** @ignore */
	    Public._construct = function(group, name, version) {
	    	if (arguments.length == 1) {
	    		var config = group
	    		if (Sincerity.Objects.isString(config)) {
		    		var parts = config.split(':')
			    	this.options = [{
			    		group: parts[0] || '*',
				    	name: parts[1] || '*',
				    	version: parts[2] || '*'
			    	}]
	    		}
	    		else {
	    			this.options = [{
	    				group: config.group || '*',
	    				name: config.name || '*',
	    				version: config.version || '*'
	    			}]
	    		}
	    	}
	    	else {
		    	this.options = [{
		    		group: group || '*',
		    		name: name || '*',
		    		version: version || '*'
		    	}]
	    	}
	    }

	    Public.isSuitableModuleIdentifer = function(moduleIdentifier) {
	    	for (var o in this.options) {
	    		var option = this.options[o]
	    		
    			if ((option.group != moduleIdentifier.group) || (option.name != moduleIdentifier.name)) { 
    				continue
    			}

	    		if (Sincerity.Dependencies.Versions.isSpecificConstraint(option.version)) {
		    		if (option.version == moduleIdentifier.version) {
		    			return true
		    		}
	    		}
	    		else {
	    			if (Sincerity.Objects.endsWith(option.version, '+')) {
	    				return true // todo
	    			}
	    			return Sincerity.Objects.matchSimple(moduleIdentifier.version, option.version)
	    		}
	    	}
	    	return false
	    }

	    Public.toString = function() {
	    	var r = 'maven:['
	    	for (var o in this.options) {
	    		var option = this.options[o]
	    		r += option.group + ':' + option.name + ':' + option.version
	    		if (o < this.options.length - 1) {
	    			r += '|'
	    		}
	    	}
	    	r += ']'
	    	return r
	    }

	    /**
	     * Get all the options that match one or more of the parameters.
	     * <p>
	     * Parameters may include one or more '*' or '?' wildcards to match any content.
		 * Escape '*' or '?' using a preceding '\'.
		 * An empty pattern matches everything.
	     * 
	     * @param {String} [group]
	     * @param {String} [name]
	     * @param {String} [version]
	     */
	    Public.getOptions = function(group, name, version) {
	    	var options = []
	    	for (var o in this.options) {
	    		var option = this.options[o]
	    		var matches = true
	    		if (Sincerity.Objects.exists(group)) {
	    			matches = Sincerity.Objects.matchSimple(option.group, group)
	    		}
	    		if (matches && Sincerity.Objects.exists(name)) {
	    			matches = Sincerity.Objects.matchSimple(option.name, name)
	    		}
	    		if (matches && Sincerity.Objects.exists(version)) {
	    			matches = Sincerity.Objects.matchSimple(option.version, version)
	    		}
	    		if (matches) {
	    			options.push(option)
	    		}
	    	}
	    	return options
	    }

	    /**
	     * Rewrites all options that match the group and/or name to a specific version.
	     * <p>
	     * Parameters may include one or more '*' or '?' wildcards to match any content.
		 * Escape '*' or '?' using a preceding '\'.
		 * An empty pattern matches everything.
	     * 
	     * @param {String} [group]
	     * @param {String} [name]
	     * @param {String} newVersion
	     * @returns {Boolean} true if any options were rewritten
	     */
	    Public.rewriteVersion = function(group, name, newVersion) {
			var options = this.getOptions(group, name)
			if (!options.length) {
				return false
			}
			for (var o in options) {
				var option = options[o]
				option.version = newVersion
			}
			return true
	    }
	    
	    /**
	     * Rewrites all options that match the group and/or name to a new group and/or name.
	     * <p>
	     * Parameters may include one or more '*' or '?' wildcards to match any content.
		 * Escape '*' or '?' using a preceding '\'.
		 * An empty pattern matches everything.
	     * 
	     * @param {String} [group]
	     * @param {String} [name]
	     * @param {String} [newGroup]
	     * @param {String} [newName]
	     * @returns {Boolean} true if any options were rewritten
	     */
	    Public.rewriteGroupName = function(group, name, newGroup, newName) {
			var options = this.getOptions(group, name)
			if (!options.length) {
				return false
			}
			for (var o in options) {
				var option = options[o]
				if (Sincerity.Objects.exists(newGroup)) {
					option.group = newGroup
				}
				if (Sincerity.Objects.exists(newName)) {
					option.name = newName
				}
			}
			return true
	    }
	    
	    return Public
	}())

	/**
	 * @class
	 * @name Sincerity.Dependencies.Maven.Repository
	 */
	Public.Repository = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.Maven.Repository */
	    var Public = {}
	    
	    /** @ignore */
	    Public._inherit = Sincerity.Dependencies.Repository
	    
	    /** @ignore */
	    Public._construct = function(config) {
	    	this.uri = config.uri
	    	this.allowMd5 = true

	    	// Remove trailing slash
	    	if (Sincerity.Objects.endsWith(this.uri, '/')) {
	    		this.uri = this.uri.substring(0, this.uri.length - 1)
	    	}
	    }
	    
	    Public.hasModule = function(moduleIdentifier) {
	    	var uri = this.getUri(moduleIdentifier, 'pom')
	    	try {
	    		uri.toURL().openStream().close()
		    	return true
	    	}
	    	catch (x) {
	    		return false
	    	}
	    }

	    Public.getModule = function(moduleIdentifier) {
    		var pom = this.getPom(moduleIdentifier)
    		if (!Sincerity.Objects.exists(pom)) {
    			return null
    		}
    		var module = new Sincerity.Dependencies.Module()
    		module.identifier = moduleIdentifier
    		var dependencyModuleConstraints = pom.getDependencyModuleConstraints()
    		for (var d in dependencyModuleConstraints) {
    			var constraints = dependencyModuleConstraints[d]
    			var dependencyModule = new Sincerity.Dependencies.Module()
    			dependencyModule.constraints = constraints
    			module.dependencies.push(dependencyModule)
    		}
    		return module
	    }

	    Public.getSuitableModuleIdentifiers = function(moduleConstraints) {
	    	var suitableModuleIdentifiers = []
	    	
	    	for (var o in moduleConstraints.options) {
	    		var option = moduleConstraints.options[o]
	    		
	    		if (Sincerity.Dependencies.Versions.isSpecificConstraint(option.version)) {
	    			// When the constraint is specific, we can skip the metadata analysis
		    		var moduleIdentifier = new Sincerity.Dependencies.Maven.ModuleIdentifier(option.group, option.name, option.version)
		    		if (this.hasModule(moduleIdentifier)) {
		    			Sincerity.Objects.pushUnique(suitableModuleIdentifiers, moduleIdentifier)
		    		}
	    		}
	    		else {
	    			var metadata = this.getMetaData(option.group, option.name) // todo: cache
	    			if (metadata) {
	    				var moduleIdentifiers = metadata.getModuleIdentifiers()
	    				suitableModuleIdentifiers = Sincerity.Objects.concatUnique(suitableModuleIdentifiers, moduleIdentifiers)
	    			}
	    		}
	    	}
	    	
			suitableModuleIdentifiers = moduleConstraints.getSuitableModuleIdentifiers(suitableModuleIdentifiers)
	    	return suitableModuleIdentifiers
	    }

	    Public.fetchModule = function(moduleIdentifier, file) {
	    	var uri = this.getUri(moduleIdentifier, 'jar')
	    	var signature = this.getSignature(uri)
	    	file = (Sincerity.Objects.isString(file) ? new java.io.File(file) : file).canonicalFile
	    	Sincerity.IO.download(uri, file)
	    	if (!this.isSignatureValid(file, signature)) {
	    		file['delete']()
	    		throw ':('
	    	}
	    }

	    Public.applyModuleRule = function(module, rule) {
			if (rule.type == 'maven') {
				if (rule.rule == 'rewriteVersion') {
					module.constraints.rewriteVersion(rule.group, rule.name, rule.newVersion)
					return true
				}
			}
			return false
	    }

	    Public.toString = function() {
	    	return 'maven:' + this.uri
	    }
	    
	    /**
	     * The Maven repository URI structure.  
	     */
	    Public.getUri = function(moduleIdentifier, extension) {
	    	var uri = this.uri
	    	
	    	var parts = moduleIdentifier.group.split('.')
	    	for (var p in parts) {
	    		var part = parts[p]
	    		uri += '/' + part
	    	}

    		uri += '/' + moduleIdentifier.name
    		uri += '/' + moduleIdentifier.version
    		uri += '/' + moduleIdentifier.name + '-' + moduleIdentifier.version + '.' + extension

	    	return new java.net.URI(uri)
	    }
	    
	    Public.getMetaDataUri = function(group, name) {
	    	var uri = this.uri
	    	
	    	var parts = group.split('.')
	    	for (var p in parts) {
	    		var part = parts[p]
	    		uri += '/' + part
	    	}

    		uri += '/' + name
    		uri += '/maven-metadata.xml'

	    	return new java.net.URI(uri)
	    }
	    
	    Public.getSignature = function(uri) {
	    	// Try sha1 first
	    	var type = 'sha1', content
	    	var signatureUri = uri + '.' + type
	    	try {
	    		content = Sincerity.IO.loadText(signatureUri)
	    	}
	    	catch (x) {
	    		if (this.allowMd5) {
		    		// Fallback to md5
	    			type = 'md5'
		    		signatureUri = uri + '.' + type
		    		content = Sincerity.IO.loadText(signatureUri)
	    		}
	    		else {
	    			throw x
	    		}
	    	}
	    	return {
	    		type: type,
	    		content: content 
	    	}
	    }
	    
	    Public.isSignatureValid = function(content, signature) {
	    	var algorithm = signature.type
	    	if (algorithm === 'sha1') {
	    		algorithm = 'SHA-1'
	    	}
	    	else if (algorithm === 'md5') {
	    		algorithm = 'MD5'
	    	}
	    	var digest = content instanceof java.io.File ? Sincerity.Cryptography.fileDigest(content, algorithm, 'hex') : Sincerity.Cryptography.bytesDigest(content, 1, algorithm, 'hex')
			return digest.toLowerCase() == signature.content.toLowerCase()
	    }
	    
	    Public.getPom = function(moduleIdentifier) {
	    	var uri = this.getUri(moduleIdentifier, 'pom')
	    	try {
		    	var signature = this.getSignature(uri)
		    	var bytes = Sincerity.IO.loadBytes(uri)
		    	if (!this.isSignatureValid(bytes, signature)) {
		    		return null
		    	}
		    	var text = Sincerity.JVM.fromBytes(bytes)
		    	var xml = Sincerity.XML.from(text)
		    	var pom = new Sincerity.Dependencies.Maven.POM(xml)
		    	if (moduleIdentifier.isEqual(pom.getModuleIdentifier())) {
		    		// Make sure this is a valid POM
		    		return pom
		    	}
	    	}
	    	catch (x) {}
	    	return null
	    }
	    
	    Public.getMetaData = function(group, name) {
	    	var uri = this.getMetaDataUri(group, name)
	    	try {
		    	var signature = this.getSignature(uri)
		    	var bytes = Sincerity.IO.loadBytes(uri)
		    	if (!this.isSignatureValid(bytes, signature)) {
		    		return null
		    	}
		    	var text = Sincerity.JVM.fromBytes(bytes)
		    	var xml = Sincerity.XML.from(text)
		    	var metadata = new Sincerity.Dependencies.Maven.MetaData(xml)
		    	if ((group == metadata.groupId) && (name == metadata.artifactId)) {
		    		// Make sure this is a valid metadata
		    		return metadata
		    	}
	    	}
	    	catch (x) {}
	    	return null
	    }

	    return Public
	}())
	
	/**
	 * @class
	 * @name Sincerity.Dependencies.Maven.POM
	 */
	Public.POM = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.Maven.POM */
	    var Public = {}
	    
	    Public._construct = function(xml) {
	    	var project = xml.getElements('project')[0]
	    	
	    	this.groupId = project.getElements('groupId')[0].getText()
	    	this.artifactId = project.getElements('artifactId')[0].getText()
	    	this.version = project.getElements('version')[0].getText()
	    	this.name = project.getElements('name')[0].getText()
	    	this.description = project.getElements('description')[0].getText()
	    	this.dependencies = []

	    	try {
		    	var dependencies = project.getElements('dependencies')[0].getElements('dependency')
		    	for (var d in dependencies) {
		    		var dependency = dependencies[d]
		    		this.dependencies.push({
		    			groupId: dependency.getElements('groupId')[0].getText(),
		    			artifactId: dependency.getElements('artifactId')[0].getText(),
		    			version: dependency.getElements('version')[0].getText()
		    		})
		    	}
	    	}
	    	catch (x) {}
	    }
	    
	    Public.getModuleIdentifier = function() {
	    	return new Sincerity.Dependencies.Maven.ModuleIdentifier(this.groupId, this.artifactId, this.version)
	    }
	    
	    Public.getDependencyModuleConstraints = function() {
	    	var moduleConstraints = []
	    	for (var d in this.dependencies) {
	    		var dependency = this.dependencies[d]
	    		moduleConstraints.push(new Sincerity.Dependencies.Maven.ModuleConstraints(dependency.groupId, dependency.artifactId, dependency.version))
	    	}
	    	return moduleConstraints
	    }

	    return Public
	}())

	/**
	 * @class
	 * @name Sincerity.Dependencies.Maven.MetaData
	 */
	Public.MetaData = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.Maven.MetaData */
	    var Public = {}
	    
	    Public._construct = function(xml) {
	    	var metadata = xml.getElements('metadata')[0]
	    	var versioning = metadata.getElements('versioning')[0]
	    	
	    	this.groupId = metadata.getElements('groupId')[0].getText()
	    	this.artifactId = metadata.getElements('artifactId')[0].getText()
	    	this.release = versioning.getElements('release')[0].getText()
	    	this.versions = []

	    	try {
		    	var versions = versioning.getElements('versions')[0].getElements('version')
		    	for (var v in versions) {
		    		var version = versions[v]
		    		this.versions.push(version.getText())
		    	}
	    	}
	    	catch (x) {}
	    }

	    Public.getModuleIdentifier = function() {
	    	return new Sincerity.Dependencies.Maven.ModuleIdentifier(this.groupId, this.artifactId, this.release)
	    }

	    Public.getModuleIdentifiers = function() {
	    	var moduleIdentifiers = []
	    	for (var v in this.versions) {
	    		var version = this.versions[v]
	    		var moduleIdentifier = new Sincerity.Dependencies.Maven.ModuleIdentifier(this.groupId, this.artifactId, version)
	    		moduleIdentifiers.push(moduleIdentifier)
	    	}
	    	return moduleIdentifiers
	    }

	    return Public
	}())

	return Public
}()

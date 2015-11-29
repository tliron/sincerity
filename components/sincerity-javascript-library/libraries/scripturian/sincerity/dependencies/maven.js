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
	'/sincerity/jvm/',
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
 * <p>
 * Additionally, pattern matching ("*", "?") is supported, as well as exclusions ("!"). 
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
	Public.ModuleIdentifier = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Maven.ModuleIdentifier */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Sincerity.Dependencies.ModuleIdentifier
	    
	    /** @ignore */
	    Public._construct = function(group, name, version) {
	    	if (arguments.length === 1) {
	    		if (Sincerity.Objects.isString(group)) {
		    		var parts = group.split(':')
		    		this.group = Sincerity.Objects.trim(parts[0])
			    	this.name = Sincerity.Objects.trim(parts[1])
			    	this.version = Sincerity.Objects.trim(parts[2])
	    		}
	    		else {
			    	this.group = Sincerity.Objects.trim(group.group)
			    	this.name = Sincerity.Objects.trim(group.name)
			    	this.version = Sincerity.Objects.trim(group.version)
	    		}
	    	}
	    	else {
		    	this.group = Sincerity.Objects.trim(group)
		    	this.name = Sincerity.Objects.trim(name)
		    	this.version = Sincerity.Objects.trim(version)
	    	}
	    	
	    	// After resolve
	    	this.uri = null
	    }

		Public.compare = function(moduleIdentifier) {
			if ((this.group == moduleIdentifier.group) && (this.name == moduleIdentifier.name)) {
				return Module.Versions.compare(this.version, moduleIdentifier.version)
			}
			else {
				return NaN
			}
		}

		Public.clone = function() {
			return new Sincerity.Dependencies.Maven.ModuleIdentifier(this)
		}

	    Public.toString = function() {
	    	return 'maven:' + this.group + ':' + this.name + ':' + this.version
	    }
	
	    return Public
	}(Public))

	/**
	 * Maven specification with support for version ranges.
	 * <p>
	 * Note that a Maven version range can in fact contain several ranges, in which case they match via a logical or.
	 * For example. "(,1.1),(1.1,)" means that everything except "1.1" will match.
	 * <p>
	 * Likewise, you may have a specification with more than one option, which will also match via a logical or,
	 * <i>unless</i> the option has a version beginning with a "!". That signifies an exclusion, which will
	 * always take precedence. For example, "!1.1" will explicitly reject "1.1", even if "1.1" is matched by
	 * other options.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.Maven.ModuleSpecification
	 */
	Public.ModuleSpecification = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Maven.ModuleSpecification */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Sincerity.Dependencies.ModuleSpecification
	    
	    /** @ignore */
	    Public._construct = function(group, name, version) {
	    	this.options = []
	    	if (arguments.length) {
	    		this.addOption.apply(this, arguments)
	    	}
	    }

	    Public.isEqual = function(moduleSpecification) {
	    	if (this.options.length != moduleSpecification.options.length) {
	    		return false
	    	}
	    	
	    	for (var o in this.options) {
	    		var option1 = this.options[o]
	    		var option2 = moduleSpecification.options[o]
    			if ((option1.group != option2.group) || (option1.name != option2.name) || (option1.version != option2.version)) {
    				return false
    			}
	    	}
	    	
	    	return true
	    }

	    Public.allowsModuleIdentifier = function(moduleIdentifier) {
	    	var allowed = false
	    	
	    	for (var o in this.options) {
	    		var option = this.options[o]
	    		
    			if ((option.group != moduleIdentifier.group) || (option.name != moduleIdentifier.name)) { 
    				continue
    			}

    			var version = option.version
    			var exclude = false
    			if (version.charAt(0) == '!') {
    				version = version.substring(1)
    				exclude = true
    			}
    			
    			if (allowed && !exclude) {
    				continue // logical or: we're already in, no need to check another option, *unless* it's an exclusion
    			}

	    		if (Module.Versions.isSpecific(version)) {
	    			allowed = (version == moduleIdentifier.version)
	    		}
	    		else {
	    			var ranges = Module.Versions.parseRanges(version)
	    			if (ranges) {
	    				allowed = Module.Versions.inRanges(moduleIdentifier.version, ranges)
	    			}
	    			else {
	    				allowed = Sincerity.Objects.matchSimple(moduleIdentifier.version, version)
	    			}
	    		}
    			
    			if (allowed && exclude) {
    				return false // exclusions take precedence
    			}
	    	}

	    	return allowed
	    }

		Public.clone = function() {
			var moduleSpecification = new Sincerity.Dependencies.Maven.ModuleSpecification()
	    	for (var o in this.options) {
	    		var option = this.options[o]
	    		moduleSpecification.addOption(option)
	    	}
			return moduleSpecification
		}

	    Public.toString = function() {
	    	var r = 'maven:{'
	    	for (var o in this.options) {
	    		var option = this.options[o]
	    		r += option.group + ':' + option.name + ':' + option.version
	    		if (o < this.options.length - 1) {
	    			r += '|'
	    		}
	    	}
	    	r += '}'
	    	return r
	    }

	    /**
	     * Adds an option to the specification.
	     * 
	     * @param {String|Object} group The group, or a config, or a complete option string
	     * @param {String} [name] The name
	     * @param {String} [version] The version
	     */
	    Public.addOption = function(group, name, version) {
	    	if (arguments.length === 1) {
	    		var config = group
	    		if (Sincerity.Objects.isString(config)) {
		    		var parts = config.split(':')
			    	this.options.push({
			    		group: Sincerity.Objects.trim(parts[0]) || '*',
				    	name: Sincerity.Objects.trim(parts[1]) || '*',
				    	version: parseVersion(parts[2])
			    	})
	    		}
	    		else {
	    			this.options.push({
	    				group: Sincerity.Objects.trim(config.group) || '*',
	    				name: Sincerity.Objects.trim(config.name) || '*',
	    				version: parseVersion(config.version)
	    			})
	    		}
	    	}
	    	else {
		    	this.options.push({
		    		group: Sincerity.Objects.trim(group) || '*',
		    		name: Sincerity.Objects.trim(name) || '*',
		    		version: parseVersion(version)
		    	})
	    	}
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
	    Public.rewrite = function(group, name, newGroup, newName) {
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
			newVersion = parseVersion(newVersion)
			for (var o in options) {
				var option = options[o]
				option.version = newVersion
			}
			return true
	    }
	    
	    function parseVersion(version) {
	    	if (!version) {
	    		return '*'
	    	}
	    	version = Sincerity.Objects.trim(version)
	    	if (!version.length) {
	    		return '*'
	    	}
	    	if (Sincerity.Objects.endsWith(version, '+')) {
	    		return '[' + version.substring(0, version.length - 1) + ',)'
	    	}
	    	return version
	    }
	    
	    return Public
	}(Public))

	/**
	 * @class
	 * @name Sincerity.Dependencies.Maven.Repository
	 */
	Public.Repository = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Maven.Repository */
	    var Public = {}
	    
	    /** @ignore */
	    Public._inherit = Sincerity.Dependencies.Repository
	    
	    /** @ignore */
	    Public._construct = function(config) {
	    	this.uri = config.uri
	    	this.checkSignatures = Sincerity.Objects.ensure(config.checkSignatures, true)
	    	this.allowMd5 = Sincerity.Objects.ensure(config.allowMd5, false)

	    	// Remove trailing slash
	    	if (Sincerity.Objects.endsWith(this.uri, '/')) {
	    		this.uri = this.uri.substring(0, this.uri.length - 1)
	    	}
	    	
	    	arguments.callee.overridden.call(this, config)
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

	    Public.getModule = function(moduleIdentifier, resolver) {
    		var pom = this.getPom(moduleIdentifier, resolver) // TODO: cache poms!
    		if (!Sincerity.Objects.exists(pom)) {
    			return null
    		}
    		var module = new Sincerity.Dependencies.Module()
    		module.identifier = moduleIdentifier
    		module.repository = this
    		for (var m in pom.dependencyModuleSpecifications) {
    			var dependencyModuleSpecification = pom.dependencyModuleSpecifications[m]
    			var dependencyModule = new Sincerity.Dependencies.Module()
    			dependencyModule.repository = this
    			dependencyModule.specification = dependencyModuleSpecification
    			dependencyModule.addReason(module)
    			module.dependencies.push(dependencyModule)
    		}
    		return module
	    }

	    Public.getAllowedModuleIdentifiers = function(moduleSpecification, resolver) {
	    	var allowedModuleIdentifiers = []
	    	
	    	for (var o in moduleSpecification.options) {
	    		var option = moduleSpecification.options[o]
	    		
	    		if (Module.Versions.isSpecific(option.version)) {
	    			// When the version is specific, we can skip the metadata analysis
		    		var moduleIdentifier = new Module.ModuleIdentifier(option.group, option.name, option.version)
		    		if (this.hasModule(moduleIdentifier)) {
		    			Sincerity.Objects.pushUnique(allowedModuleIdentifiers, moduleIdentifier)
		    		}
	    		}
	    		else {
	    			var metadata = this.getMetaData(option.group, option.name, resolver) // TODO: cache metadata!
	    			if (metadata) {
	    				allowedModuleIdentifiers = Sincerity.Objects.concatUnique(allowedModuleIdentifiers, metadata.moduleIdentifiers)
	    			}
	    		}
	    	}
	    	
			allowedModuleIdentifiers = moduleSpecification.getAllowedModuleIdentifiers(allowedModuleIdentifiers)
	    	return allowedModuleIdentifiers
	    }
	    
	    Public.fetchModule = function(moduleIdentifier, directory, overwrite, resolver) {
	    	var uri = this.getUri(moduleIdentifier, 'jar')
	    	var file = this.getModuleFile(moduleIdentifier, directory)
	    	var signature = this.getSignature(uri)
	    	
	    	if (overwrite || !file.exists()) {
		    	file.parentFile.mkdirs()
		    	var id = Sincerity.Objects.uniqueString()
		    	resolver.eventHandler.handleEvent({type: 'begin', id: id, progress: 0, message: 'Downloading: ' + uri})
		    	Sincerity.IO.download(uri, file)
		    	for (var i = 0; i <= 100; i += 10) {
			    	resolver.eventHandler.handleEvent({type: 'update', id: id, progress: i / 100})
		    		Sincerity.JVM.sleep(100) // :)
		    	}
		    	resolver.eventHandler.handleEvent({type: 'end', id: id, message: 'Downloaded: ' + uri})
	    	}

	    	var id = Sincerity.Objects.uniqueString()
	    	resolver.eventHandler.handleEvent({type: 'begin', id: id, message: 'Verifying: ' + file})
	    	Sincerity.JVM.sleep(300) // :)
	    	if (this.isSignatureValid(file, signature)) {
		    	resolver.eventHandler.handleEvent({type: 'end', id: id, message: 'Verified: ' + file})
	    	}
	    	else {
		    	resolver.eventHandler.handleEvent({type: 'end', id: id, message: 'File does not match signature: ' + file})
	    		file['delete']()
	    		// throw ':('
	    	}
	    }

	    Public.applyModuleRule = function(module, rule) {
			if (rule.platform == 'maven') {
				if (rule.type == 'exclude') {
					var options = module.specification.getOptions(rule.group, rule.name)
					if (options.length) {
						return 'exclude'
					}
				}
				else if (rule.type == 'excludeDependencies') {
					var options = module.specification.getOptions(rule.group, rule.name)
					if (options.length) {
						return 'excludeDependencies'
					}
				}
				else if (rule.type == 'rewrite') {
					module.specification.rewrite(rule.group, rule.name, rule.newGroup, rule.newName)
					return true
				}
				else if (rule.type == 'rewriteVersion') {
					module.specification.rewriteVersion(rule.group, rule.name, rule.newVersion)
					return true
				}
			}
			return null
	    }

		Public.clone = function() {
			return new Sincerity.Dependencies.Maven.Repository(this)
		}

		Public.toString = function() {
	    	return 'uri=maven:' + this.uri + ', checkSignatures=' + this.checkSignatures + ', allowMd5=' + this.allowMd5
	    }

	    Public.getModuleFile = function(moduleIdentifier, directory) {
	    	directory = (Sincerity.Objects.isString(directory) ? new java.io.File(directory) : directory).canonicalFile
	    	var file = directory
	    	file = new java.io.File(file, moduleIdentifier.group)
	    	file = new java.io.File(file, moduleIdentifier.name)
	    	file = new java.io.File(file, moduleIdentifier.version)
	    	file = new java.io.File(file, moduleIdentifier.name + '.jar')
	    	return file
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
	    	if (!this.checkSignatures) {
	    		// TODO: warning
	    		return null
	    	}

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
	    	if (!this.checkSignatures) {
	    		// TODO: warning
	    		return true
	    	}
	    	
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

	    Public.getPom = function(moduleIdentifier, resolver) {
	    	var uri = this.getUri(moduleIdentifier, 'pom')
	    	try {
		    	var signature = this.getSignature(uri)
		    	var bytes = Sincerity.IO.loadBytes(uri)
		    	if (!this.isSignatureValid(bytes, signature)) {
		    		return null
		    	}
		    	var text = Sincerity.JVM.fromBytes(bytes)
		    	var xml = Sincerity.XML.from(text)
		    	var pom = new Module.POM(xml)
		    	if (moduleIdentifier.compare(pom.moduleIdentifier) === 0) {
		    		// Make sure this is a valid POM
		    		return pom
		    	}
	    	}
	    	catch (x) {
	    		resolver.eventHandler.handleEvent({type: 'error', message: 'Could not get POM: ' + uri})
	    	}
	    	return null
	    }
	    
	    Public.getMetaData = function(group, name, resolver) {
	    	var uri = this.getMetaDataUri(group, name)
	    	try {
		    	var signature = this.getSignature(uri)
		    	var bytes = Sincerity.IO.loadBytes(uri)
		    	if (!this.isSignatureValid(bytes, signature)) {
		    		return null
		    	}
		    	var text = Sincerity.JVM.fromBytes(bytes)
		    	var xml = Sincerity.XML.from(text)
		    	var metadata = new Module.MetaData(xml)
		    	if ((group == metadata.groupId) && (name == metadata.artifactId)) {
		    		// Make sure this is a valid metadata
		    		return metadata
		    	}
	    	}
	    	catch (x) {
	    		resolver.eventHandler.handleEvent({type: 'error', message: 'Could not get metadata: ' + uri})
	    	}
	    	return null
	    }

	    return Public
	}(Public))
	
	/**
	 * @class
	 * @name Sincerity.Dependencies.Maven.POM
	 */
	Public.POM = Sincerity.Classes.define(function(Module) {
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
		    		
		    		function getOptional(name) {
		    			var elements = dependency.getElements(name)
		    			return elements.length ? elements[0].getText() : null
		    		}
		    		
		    		this.dependencies.push({
		    			groupId: dependency.getElements('groupId')[0].getText(),
		    			artifactId: dependency.getElements('artifactId')[0].getText(),
		    			version: dependency.getElements('version')[0].getText(),
		    			type: getOptional('type'),
		    			scope: getOptional('scope')
		    		})
			    	// TODO: take into account <type> and <scope>
		    	}
	    	}
	    	catch (x) {}
	    	
	    	// Parse
	    	this.moduleIdentifier = new Module.ModuleIdentifier(this.groupId, this.artifactId, this.version)
	    	this.dependencyModuleSpecifications = []
	    	for (var d in this.dependencies) {
	    		var dependency = this.dependencies[d]
	    		this.dependencyModuleSpecifications.push(new Module.ModuleSpecification(dependency.groupId, dependency.artifactId, dependency.version))
	    	}
	    }
	    
	    return Public
	}(Public))

	/**
	 * @class
	 * @name Sincerity.Dependencies.Maven.MetaData
	 */
	Public.MetaData = Sincerity.Classes.define(function(Module) {
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
	    	catch (x) { println(x) }
	    	
	    	// Parse
	    	this.moduleIdentifier = new Module.ModuleIdentifier(this.groupId, this.artifactId, this.release)
	    	this.moduleIdentifiers = []
	    	for (var v in this.versions) {
	    		var version = this.versions[v]
	    		var moduleIdentifier = new Module.ModuleIdentifier(this.groupId, this.artifactId, version)
	    		this.moduleIdentifiers.push(moduleIdentifier)
	    	}
	    }

	    return Public
	}(Public))

	/**
	 * Utilities for working with Maven versions.
	 * 
	 * @namespace
	 */
	Public.Versions = function(Module) {
		/** @exports Public as Sincerity.Dependencies.Maven */
		var Public = {}
		
	    /**
	     * Checks if the version is specific.
	     * <p>
	     * Resolving a non-specific version would require fetching metadata.
	     * 
	     * @param {String} version
	     * @returns {Boolean} true if specific
	     */
	    Public.isSpecific = function(version) {
			if (!version.length) {
				return false
			}
			if (version.indexOf('*') != -1) {
				return false
			}
			if (version.indexOf('?') != -1) {
				return false
			}
			var first = version.charAt(0)
			if ((first == '!') || (first == '[') || (first == '(')) {
				return false
			}
	    	return true
	    }
	    
		/**
		 * Compares two versions.
		 * <p>
		 * Versions should have forms such as '1.0' or '2.4.1-beta1'.
		 * <p>
		 * The comparison first takes into account the dot-separated integer parts.
		 * In case both versions are identical on those terms, then the postfix after
		 * the dash is compared.
		 * <p>
		 * Postfix comparison takes into account its semantic meaning. Thus,
		 * 'beta2' would be greater than 'alpha3', and 'alpha3' would be greater than
		 * 'dev12'. 
		 * 
	     * @param {String} version1
	     * @param {String} version2
		 * @returns {Number} -1 if version2 is greater, 0 if equal, 1 if version1 is greater
		 */
	    Public.compare = function(version1, version2) {
	    	if (version1 == version2) { // optimization for trivial equality
	    		return 0
	    	}
	    	
			version1 = Public.parse(version1)
			version2 = Public.parse(version2)
			
			var length1 = version1.parts.length
			var length2 = version2.parts.length
			var length = Math.max(length1, length2)
			for (var p = 0; p < length; p++) {
				var part1 = p <= length1 - 1 ? version1.parts[p] : null
				var part2 = p <= length2 - 1 ? version2.parts[p] : null
				if ((null === part1) && (null === part2)) {
					return 0
				}
				if (null === part1) {
					return -1
				}
				if (null === part2) {
					return 1
				}
				if (part1 != part2) {
					return part1 - part2 > 0 ? 1 : -1
				}
				// Equal, so continue
			}
			
			if (version1.extra != version2.extra) {
				return version1.extra - version2.extra > 0 ? 1 : -1
			}
		
			return 0
		}
		
		/**
		 * Utility to parse a version string into parts that can be compared.
		 * 
		 * @param {String} version
		 * @returns {Object}
		 */
		Public.parse = function(version) {
			version = Sincerity.Objects.trim(version)
			var dash = version.indexOf('-')
			var main = dash === -1 ? version : version.substring(0, dash)
			var postfix = dash === -1 ? '' : version.substring(dash + 1)
					
			var parts = main.length ? main.split('.') : []
			for (var p in parts) {
				parts[p] = parseInt(parts[p])
			}
			
			var postfixFirstDigit = postfix.search(/\d/)
			var postfixMain = postfixFirstDigit === -1 ? postfix : postfix.substring(0, postfixFirstDigit)
			var postfixNumber = postfixFirstDigit === -1 ? 0 : parseInt(postfix.substring(postfixFirstDigit)) / 10
			var extra = postfixMain.length ? Public.parsePostfix(postfixMain) : 0
			extra += postfixNumber
			
			return {
				parts: parts,
				extra: extra
			}
		}
	
		/**
		 * Utility to convert a version postfix into a number that can be compared.
		 * 
		 * @param {String} version
		 * @returns {Number}
		 */
		Public.parsePostfix = function(postfix) {
			postfix = postfix.toLowerCase()
			return Public.postfixes[postfix] || 0
		}
		
		/**
		 * Parses a ranges specification, e.g '[1.2,2.0),[3.0,)'.
		 * 
		 * @param {String} version
		 * @returns {Object}
		 */
		Public.parseRanges = function(version) {
	    	version = Sincerity.Objects.trim(version)
	    	
	    	var rangeRegExp = /[\[\(]\s*([^,\s]*)\s*,\s*([^,\]\)\s]*)\s*[\]\)]/g
	    	var matches = rangeRegExp.exec(version)
	    	if (null === matches) {
	    		return null
	    	}
			
			var ranges = []
			
			while (null !== matches) {
				var lastIndex = rangeRegExp.lastIndex
				var start = matches[1]
				var end = matches[2]
				var open = version.charAt(matches.index)
				var close = version.charAt(lastIndex - 1)
				
				ranges.push({
					start: start,
					end: end,
					includeStart: open == '[',
					includeEnd: close == ']'
				})
				
				matches = rangeRegExp.exec(version)

				if (null !== matches) {
					// Make sure there is a comma in between ranges
					var between = version.substring(lastIndex, matches.index)
					if (!/^\s+,\s+$/.test(between)) {
						return null
					}
				}
			}
			
			return ranges
	    }
	    
		Public.inRanges = function(version, ranges) {
	    	for (var r in ranges) {
	    		var range = ranges[r]
				var compareStart = range.start ? Public.compare(version, range.start) : 1
				var compareEnd = range.end ? Public.compare(range.end, version) : 1
				//println(version + (compareStart == 0 ? '=' : (compareStart > 0 ? '>' : '<')) + range.start)
				//println(version + (compareEnd == 0 ? '=' : (compareEnd > 0 ? '<' : '>')) + range.end)
				if (range.includeStart && range.includeEnd) {
					match = (compareStart >= 0) && (compareEnd >= 0) 
				}
				else if (range.includeStart && !range.includeEnd) {
					match = (compareStart >= 0) && (compareEnd > 0) 
				}
				else if (!range.includeStart && range.includeEnd) {
					match = (compareStart > 0) && (compareEnd >= 0) 
				}
				else {
					match = (compareStart > 0) && (compareEnd > 0)
				}
				if (match) {
		    		//println(version + ' in ' + (range.includeStart ? '[' : '(') + range.start + ',' + range.end + (range.includeEnd ? ']' : ')'))
					return true // logical or: it takes just one positive to be positive
				}
				//else println(version + ' not in ' + (range.includeStart ? '[' : '(') + range.start + ',' + range.end + (range.includeEnd ? ']' : ')'))
	    	}
	    	return false
	    }
		
		Public.postfixes = {
			'd': -3,
			'dev': -3,
			'a': -2,
			'alpha': -2,
			'b': -1,
			'beta': -1
		}
	    
	    return Public
	}(Public)

	return Public
}()

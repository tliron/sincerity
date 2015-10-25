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
	'/sincerity/classes/',
	'/sincerity/objects/')

var Sincerity = Sincerity || {}

/**
 * @namespace
 */
Sincerity.Dependencies = Sincerity.Dependencies || function() {
	/** @exports Public as Sincerity.Dependencies */
	var Public = {}
	
	Public.createRepository = function(config, defaultType) {
		var className = Sincerity.Objects.capitalize(config.type || defaultType)
		var clazz = Sincerity.Dependencies[className].Repository
		return new clazz(config)
	}

	Public.createModuleConstraints = function(config, defaultType) {
		var className = Sincerity.Objects.capitalize(config.type || defaultType)
		var clazz = Sincerity.Dependencies[className].ModuleConstraints
		return new clazz(config)
	}

	/**
	 * @class
	 * @name Sincerity.Dependencies.ModuleIdentifier
	 */
	Public.ModuleIdentifier = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.ModuleIdentifier */
	    var Public = {}
	    
	    Public.isEqual = function(moduleIdentifier) {
	    	return false
	    }

	    Public.toString = function() {
	    	return ''
	    }

	    return Public
	}())
	
	/**
	 * @class
	 * @name Sincerity.Dependencies.ModuleConstraints
	 */
	Public.ModuleConstraints = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.ModuleConstraints */
	    var Public = {}
	    
	    Public.isSuitable = function(moduleIdentifier) {
	    	return false
	    }

	    Public.getSuitableModuleIdentifiers = function(moduleIdentifiers) {
	    	var suitableModuleIdentifiers = []
	    	for (var m in moduleIdentifiers) {
	    		var moduleIdentifier = moduleIdentifiers[m]
	    		if (this.isSuitable(moduleIdentifier)) {
	    			suitableModuleIdentifiers.push(moduleIdentifier)
	    		}
	    	}
	    	return suitableModuleIdentifiers
	    }

	    Public.toString = function() {
	    	return ''
	    }

	    return Public
	}())
	
	/**
	 * @class
	 * @name Sincerity.Dependencies.Module
	 */
	Public.Module = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.Module */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function() {
	    	this.identifier = null
	    	this.constraints = null
	    	this.dependencies = []
	    	this.transitive = null
	    }
	    
	    Public.toString = function() {
	    	var r = ''
	    	if (this.transitive) {
	    		r += '>'
	    	}
	    	if (Sincerity.Objects.exists(this.identifier)) {
	    		if (r.length) { r += ' ' }
	    		r += 'identifier: ' + this.identifier.toString()
	    	}
	    	if (Sincerity.Objects.exists(this.constraints)) {
	    		if (r.length) { r += ', ' }
	    		r += 'constraints: ' + this.constraints.toString()
	    	}
	    	if (this.dependencies.length) {
	    		if (r.length) { r += ', ' }
	    		r += 'dependencies: ' + this.dependencies.length
	    	}
	    	return r
	    }
	    
	    Public.dump = function(out, recursive, indent) {
	    	indent = indent || 0
	    	for (var i = indent; i > 0; i--) {
	    		out.print(' ')
	    	}
	    	out.println(this.toString())
	    	if (recursive) {
		    	for (var d in this.dependencies) {
		    		var dependency = this.dependencies[d]
		    		dependency.dump(out, true, indent + 1)
		    	}
	    	}
	    }

	    return Public
	}())

	/**
	 * @class
	 * @name Sincerity.Dependencies.Repository
	 */
	Public.Repository = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.Repository */
	    var Public = {}
	    
	    Public.hasModule = function(moduleIdentifier) {
	    	return false
	    }

	    Public.getModule = function(moduleIdentifier) {
	    	return null
	    }

	    Public.getSuitableModuleIdentifiers = function(moduleConstraints) {
	    	return []
	    }

	    Public.fetchModule = function(moduleIdentifier, file) {
	    }
	    
	    Public.applyModuleRule = function(module, rule) {
	    	return false
	    }

	    Public.toString = function() {
	    	return ''
	    }

	    return Public
	}())

	/**
	 * @class
	 * @name Sincerity.Dependencies.Resolver
	 */
	Public.Resolver = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.Resolver */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function() {
	    	this.defaultType = 'maven'
	    }

	    /**
	     * @param dependencies
	     * @param repositories
	     * @param rules
	     */
	    Public.resolve = function(dependencies, repositories, rules) {
	    	dependencies = Sincerity.Objects.clone(dependencies)
	    	repositories = Sincerity.Objects.clone(repositories)
	    	rules = Sincerity.Objects.clone(rules)
	    	
	    	for (var d in dependencies) {
	    		var dependency = dependencies[d]
	    		var module = new Sincerity.Dependencies.Module()
	    		module.constraints = Sincerity.Dependencies.createModuleConstraints(dependency, this.defaultType)
	    		module.transitive = false
	    		dependencies[d] = module
	    	}

	    	for (var r in repositories) {
	    		repositories[r] = Sincerity.Dependencies.createRepository(repositories[r], this.defaultType)
	    	}

			for (var r in rules) {
				var rule = rules[r]
				rule.type = rule.type || this.defaultType
			}

			// Resolve modules
	    	for (var d in dependencies) {
	    		var module = dependencies[d]
	    		this.resolveModule(module, repositories, rules, true)
	    	}

	    	return dependencies
	    }

	    /**
	     *
	     * @param {Sincerity.Dependencies.Module} module
	     * @param {Sincerity.Dependencies.Repository[]} repositories
	     * @param {Object[]} rules
	     * @param {Boolean} [recursive=false]
	     */
	    Public.resolveModule = function(module, repositories, rules, recursive) {
	    	// Apply rules
			for (var r in rules) {
				var rule = rules[r]
				for (var rr in repositories) {
					var repository = repositories[rr]
    				if (repository.applyModuleRule(module, rule)) {
    					continue
    				}
    			}
    		}

    		if (Sincerity.Objects.exists(module.identifier)) {
    			// Already resolved
    		}
    		else if (Sincerity.Objects.exists(module.constraints)) {
    			// Resolve
	    		for (var r in repositories) {
		    		var repository = repositories[r]
		    		var suitableModuleIdentifiers = repository.getSuitableModuleIdentifiers(module.constraints)
		    		if (suitableModuleIdentifiers.length) {
			    		suitableModuleIdentifiers.sort(function(moduleIdentifier1, moduleIdentifier2) {
			    			// Reverse version order
			    			return Sincerity.Dependencies.Versions.compare(moduleIdentifier2.version, moduleIdentifier1.version)
			    		})
			    		
		    			var bestModule = repository.getModule(suitableModuleIdentifiers[0])
		    			module.identifier = bestModule.identifier
		    			module.dependencies = bestModule.dependencies
				    	for (d in module.dependencies) {
				    		module.dependencies[d].transitive = true
				    	}
		    		}
		    	}
    		}

			if (recursive) {
				// Resolve dependencies recursively
		    	for (d in module.dependencies) {
		    		var dependency = module.dependencies[d]
		    		this.resolveModule(dependency, repositories, rules, true)
		    	}
			}
	    }

	    return Public
	}())

	/**
	 * @namespace
	 */
	Public.Versions = {
	    /**
	     * Checks if the constraint version is specific.
	     * <p>
	     * Resolving a non-specific version would require fetching metadata.
	     * 
	     * @returns {Boolean} true if specific
	     */
	    isSpecificConstraint: function(version) {
			if (!version.length) {
				return false
			}
			if (Sincerity.Objects.endsWith(version, '+')) {
				return false
			}
			if (Sincerity.Objects.startsWith(version, '[')) {
				return false
			}
			if (Sincerity.Objects.startsWith(version, '(')) {
				return false
			}
			if (version.indexOf(',') != -1) {
				return false
			}
			if (version.indexOf('*') != -1) {
				return false
			}
	    	return true
	    },
		
		/**
		 * 
		 * @returns {Number} -1 if version2 is greater, 0 if equal, 1 if version1 is greater
		 */
		compare: function(version1, version2) {
			version1 = Sincerity.Dependencies.Versions.parse(version1)
			version2 = Sincerity.Dependencies.Versions.parse(version2)
			
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
			}
			
			if (version1.extra != version2.extra) {
				return version1.extra - version2.extra > 0 ? 1 : -1
			}
		
			return 0
		},
		
		parse: function(version) {
			version = Sincerity.Objects.trim(version)
			var dash = version.indexOf('-')
			var main = dash == -1 ? version : version.substring(0, dash)
			var postfix = dash == -1 ? '' : version.substring(dash + 1)
					
			var parts = main.length ? main.split('.') : []
			for (var p in parts) {
				parts[p] = parseInt(parts[p])
			}
			
			var postfixFirstDigit = postfix.search(/\d/)
			var postfixMain = postfixFirstDigit == -1 ? postfix : postfix.substring(0, postfixFirstDigit)
			var postfixNumber = postfixFirstDigit == -1 ? 0 : parseInt(postfix.substring(postfixFirstDigit)) / 10
			var extra = postfixMain.length ? Sincerity.Dependencies.Versions.parsePostfix(postfixMain) : 0
			extra += postfixNumber
			
			return {
				parts: parts,
				extra: extra
			}
		},
	
		parsePostfix: function(postfix) {
			postfix = postfix.toLowerCase()
			if ((postfix == 'd') || (postfix == 'dev')) {
				return -3
			}
			if ((postfix == 'a') || (postfix == 'alpha')) {
				return -2
			}
			if ((postfix == 'b') || (postfix == 'beta')) {
				return -1
			}
			return 0
		}
	}

	return Public
}()


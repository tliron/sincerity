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
 * Dependency management services and utilities.
 * 
 * @namespace
 * 
 * @author Tal Liron
 */
Sincerity.Dependencies = Sincerity.Dependencies || function() {
	/** @exports Public as Sincerity.Dependencies */
	var Public = {}
	
	/**
	 * Creates a repository instance.
	 * 
	 * @param {Object} config
	 * @param {String} [config.type=defaultType] The repository type
	 * @param {String} [defaultType] Optional default type to fallback to if not specified in config
	 * @returns {Sincerity.Dependency.Repository}
	 */
	Public.createRepository = function(config, defaultType) {
		var className = Sincerity.Objects.capitalize(config.type || defaultType)
		var clazz = Sincerity.Dependencies[className].Repository
		return new clazz(config)
	}

	/**
	 * Creates a module constraints instance.
	 * 
	 * @param {Object} config
	 * @param {String} [config.type=defaultType] The module constraints type
	 * @param {String} [defaultType] Optional default type to fallback to if not specified in config
	 * @returns {Sincerity.Dependency.ModuleConstraints}
	 */
	Public.createModuleConstraints = function(config, defaultType) {
		var className = Sincerity.Objects.capitalize(config.type || defaultType)
		var clazz = Sincerity.Dependencies[className].ModuleConstraints
		return new clazz(config)
	}

	/**
	 * Base class for module identifiers.
	 * <p>
	 * These are implemented differently per repository type.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.ModuleIdentifier
	 */
	Public.ModuleIdentifier = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.ModuleIdentifier */
	    var Public = {}
	    
	    /**
	     * Compare to another module identifier. Note that the
	     * module identifiers should be the same or of compatible types.
	     * 
	     * @param {Sincerity.Dependencies.ModuleIdentifier} moduleIdentifier The module identifier to compare with us 
	     * @returns {Boolean} true if the identifiers are equal
	     */
	    Public.isEqual = function(moduleIdentifier) {
	    	return false
	    }

	    /**
	     * Represent the instance as a string.
	     * 
	     * @returns {String} A string representation
	     */
	    Public.toString = function() {
	    	return ''
	    }

	    return Public
	}())
	
	/**
	 * Base class for module constraints.
	 * <p>
	 * These are implemented differently per repository type.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.ModuleConstraints
	 */
	Public.ModuleConstraints = Sincerity.Classes.define(function() {
		/** @exports Public as Sincerity.Dependencies.ModuleConstraints */
	    var Public = {}
	    
	    /**
	     * Checks whether a module identifier matches the constraints.
	     * 
	     * @param {Sincerity.Dependencies.ModuleIdentifier} moduleIdentifier
	     * @returns {Boolean} true if the module identifier is suitable
	     * @see Sincerity.Dependencies.ModuleConstraints#getSuitableModuleIdentifiers
	     */
	    Public.isSuitableModuleIdentifer = function(moduleIdentifier) {
	    	return false
	    }

	    /**
	     * Filters out those module identifiers that match the constraints.
	     * 
	     * @param {Sincerity.Dependencies.ModuleIdentifier[]} moduleIdentifier
	     * @returns {Sincerity.Dependencies.ModuleIdentifier[]}
	     * @see Sincerity.Dependencies.ModuleConstraints#isSuitableModuleIdentifer
	     */
	    Public.getSuitableModuleIdentifiers = function(moduleIdentifiers) {
	    	var suitableModuleIdentifiers = []
	    	for (var m in moduleIdentifiers) {
	    		var moduleIdentifier = moduleIdentifiers[m]
	    		if (this.isSuitableModuleIdentifer(moduleIdentifier)) {
	    			suitableModuleIdentifiers.push(moduleIdentifier)
	    		}
	    	}
	    	return suitableModuleIdentifiers
	    }

	    /**
	     * Represent the instance as a string.
	     * 
	     * @returns {String} A string representation
	     */
	    Public.toString = function() {
	    	return ''
	    }

	    return Public
	}())

	/**
	 * Base class for repositories.
	 * 
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

	    /**
	     * Represent the instance as a string.
	     * 
	     * @returns {String} A string representation
	     */
	    Public.toString = function() {
	    	return ''
	    }

	    return Public
	}())
	
	/**
	 * Represents a single dependency.
	 * 
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
	    
	    /**
	     * Represent the instance as a string.
	     * 
	     * @returns {String} A string representation
	     */
	    Public.toString = function() {
	    	var r = '', prefix = ''
	    	if (Sincerity.Objects.exists(this.identifier)) {
	    		r += 'identifier: ' + this.identifier.toString()
	    		prefix = '*'
	    	}
	    	else {
	    		prefix = '!'
	    	}
	    	if (Sincerity.Objects.exists(this.constraints)) {
	    		if (r.length) { r += ', ' }
	    		r += 'constraints: ' + this.constraints.toString()
	    	}
	    	if (this.dependencies.length) {
	    		if (r.length) { r += ', ' }
	    		r += 'dependencies: ' + this.dependencies.length
	    	}
	    	if (this.transitive) {
	    		prefix += '>'
	    	}
	    	if (prefix.length) {
	    		r = prefix + ' ' + r
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
	 * Handles resolving and fetching of dependency tree.
	 * 
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
	     * @param {Object[]} dependencies Constraint configurations
	     * @param {Object[]} repositories Repository configurations
	     * @param {Object[]} rules Rule configurations
	     */
	    Public.resolve = function(dependencies, repositories, rules) {
	    	dependencies = Sincerity.Objects.clone(dependencies)
	    	repositories = Sincerity.Objects.clone(repositories)
	    	rules = Sincerity.Objects.clone(rules)
	    	
	    	// Create modules
	    	for (var d in dependencies) {
	    		var dependency = dependencies[d]
	    		var module = new Sincerity.Dependencies.Module()
	    		module.constraints = Sincerity.Dependencies.createModuleConstraints(dependency, this.defaultType)
	    		module.transitive = false
	    		dependencies[d] = module
	    	}

	    	// Create repositories
	    	for (var r in repositories) {
	    		repositories[r] = Sincerity.Dependencies.createRepository(repositories[r], this.defaultType)
	    	}

	    	// Adjust rules
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
	 * Utilities for working with dependency versions.
	 * 
	 * @namespace
	 */
	Public.Versions = {
	    /**
	     * Checks if the constraint version is specific.
	     * <p>
	     * Resolving a non-specific version would require fetching metadata.
	     * 
	     * @param {String} version
	     * @returns {Boolean} true if specific
	     */
	    isSpecificConstraint: function(version) {
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
	    },
	    
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
		
		/**
		 * Utility to parse a version string into parts that can be compared.
		 * 
		 * @param {String} version
		 * @returns {Object}
		 */
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
	
		/**
		 * Utility to convert a version postfix into a number that can be compared.
		 * 
		 * @param {String} version
		 * @returns {Number}
		 */
		parsePostfix: function(postfix) {
			postfix = postfix.toLowerCase()
			return Sincerity.Dependencies.Versions.postfixes[postfix] || 0
		},
		
		/**
		 * Parses a ranges constraint, e.g '[1.2,2.0),[3.0,)'.
		 * 
		 * @param {String} version
		 * @returns {Object}
		 */
	    parseRangesConstraint: function(version) {
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
	    },
	    
	    inRangesConstraint: function(version, ranges) {
	    	var match = false
	    	for (var r in ranges) {
	    		var range = ranges[r]
				var compareStart = range.start ? Sincerity.Dependencies.Versions.compare(version, range.start) : 0
				var compareEnd = range.end ? Sincerity.Dependencies.Versions.compare(range.end, version) : 0
				if (range.includeStart && range.includeEnd) {
					match = (compareStart >= 0) && (compareEnd >= 0) 
				}
				else if (range.includeStart && !range.includeEnd) {
					match = (compareStart >= 0) && (compareEnd == 0) 
				}
				else if (!range.includeStart && range.includeEnd) {
					match = (compareStart == 0) && (compareEnd >= 0) 
				}
				match = (compareStart == 0) && (compareEnd == 0)
				if (!match) {
					return false // logical and: it takes just one negative to be negative
				}
	    	}
	    	return match
	    },
		
		postfixes: {
			'd': -3,
			'dev': -3,
			'a': -2,
			'alpha': -2,
			'b': -1,
			'beta': -1
		}
	}

	return Public
}()


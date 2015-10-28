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
	'/sincerity/objects/',
	'/sincerity/jvm/')

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
		var clazz = Public[className].Repository
		return new clazz(config)
	}

	/**
	 * Creates a module specification instance.
	 * 
	 * @param {Object} config
	 * @param {String} [config.type=defaultType] The module specification type
	 * @param {String} [defaultType] Optional default type to fallback to if not specified in config
	 * @returns {Sincerity.Dependency.ModuleSpecification}
	 */
	Public.createModuleSpecification = function(config, defaultType) {
		var className = Sincerity.Objects.capitalize(config.type || defaultType)
		var clazz = Public[className].ModuleSpecification
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
	Public.ModuleIdentifier = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.ModuleIdentifier */
	    var Public = {}
	    
	    /**
	     * Compare to another module identifier. Note that the
	     * module identifiers should be of the same or of compatible types.
	     * 
	     * @param {Sincerity.Dependencies.ModuleIdentifier} moduleIdentifier The module identifier to compare with us 
	     * @returns {Boolean} true if the identifiers are equal
	     */
	    Public.isEqual = function(moduleIdentifier) {
	    	return false
	    }

		/**
		 * Compares to another module identifiers.
		 * 
	     * @param {Sincerity.Dependencies.ModuleIdentifier} moduleIdentifier The module identifier to compare with us
		 * @returns {Number} -1 if moduleIdentifier is greater, 0 if equal, 1 if we are greater
		 */
		Public.compare = function(moduleIdentifier) {
			return 0
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
	}(Public))
	
	/**
	 * Base class for module specification.
	 * <p>
	 * These are implemented differently per repository type.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.ModuleSpecification
	 */
	Public.ModuleSpecification = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.ModuleSpecification */
	    var Public = {}

	    /**
	     * Compare to another module specification. Note that the
	     * module specification should be of the same or of compatible types.
	     * 
	     * @param {Sincerity.Dependencies.ModuleSpecification} moduleSpecification The module specification to compare with us 
	     * @returns {Boolean} true if the specification are equal
	     */
	    Public.isEqual = function(moduleSpecification) {
	    	return false
	    }

	    /**
	     * Checks whether a module identifier is allowed by the specification.
	     * 
	     * @param {Sincerity.Dependencies.ModuleIdentifier} moduleIdentifier
	     * @returns {Boolean} true if the module identifier is allowed
	     * @see Sincerity.Dependencies.ModuleSpecification#getAllowedModuleIdentifiers
	     */
	    Public.allowsModuleIdentifier = function(moduleIdentifier) {
	    	return false
	    }

	    /**
	     * Filters out those module identifiers that match the specification.
	     * 
	     * @param {Sincerity.Dependencies.ModuleIdentifier[]} moduleIdentifier
	     * @returns {Sincerity.Dependencies.ModuleIdentifier[]}
	     * @see Sincerity.Dependencies.ModuleSpecification#allowsModuleIdentifier
	     */
	    Public.getAllowedModuleIdentifiers = function(moduleIdentifiers) {
	    	var allowedModuleIdentifiers = []
	    	for (var m in moduleIdentifiers) {
	    		var moduleIdentifier = moduleIdentifiers[m]
	    		if (this.allowsModuleIdentifier(moduleIdentifier)) {
	    			allowedModuleIdentifiers.push(moduleIdentifier)
	    		}
	    	}
	    	return allowedModuleIdentifiers
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
	}(Public))

	/**
	 * Base class for repositories.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.Repository
	 */
	Public.Repository = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Repository */
	    var Public = {}
	    
	    Public.hasModule = function(moduleIdentifier) {
	    	return false
	    }

	    Public.getModule = function(moduleIdentifier) {
	    	return null
	    }

	    Public.getAllowedModuleIdentifiers = function(moduleSpecification) {
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
	}(Public))
	
	/**
	 * A module can have dependencies as well as dependents.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.Module
	 */
	Public.Module = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Module */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function() {
	    	this.identifier = null
	    	this.specification = null
	    	this.dependencies = []
	    	this.dependents = []
	    }
	    
	    Public.copyFrom = function(module) {
	    	this.identifier = module.identifier
	    	this.dependencies = module.dependencies
	    }
	    
	    /**
	     * Represent the instance as a string.
	     * 
	     * @returns {String} A string representation
	     */
	    Public.toString = function() {
	    	var r = '', prefix = ''
	    		prefix += !this.dependents.length ? '*' : '+' // root?
	    	prefix += Sincerity.Objects.exists(this.identifier) ? '!' : '?' // resolved?
	    	if (Sincerity.Objects.exists(this.identifier)) {
	    		r += 'identifier: ' + this.identifier.toString()
	    	}
	    	if (Sincerity.Objects.exists(this.specification)) {
	    		if (r.length) { r += ', ' }
	    		r += 'specification: ' + this.specification.toString()
	    	}
	    	if (this.dependencies.length) {
	    		if (r.length) { r += ', ' }
	    		r += 'dependencies: ' + this.dependencies.length
	    	}
	    	if (this.dependents.length) {
	    		if (r.length) { r += ', ' }
	    		r += 'dependents: ' + this.dependents.length
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
	}(Public))

	/**
	 * Handles resolving and fetching of dependency tree.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.Resolver
	 */
	Public.Resolver = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Resolver */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function() {
	    	this.defaultType = 'maven'
	    	this.resolvedCache = Sincerity.JVM.newList(true)
	    	this.resolvedCacheHits = new java.util.concurrent.atomic.AtomicInteger()
	    }

	    /**
	     * @param {Object[]} moduleSpecifications Module specification configurations
	     * @param {Object[]} repositories Repository configurations
	     * @param {Object[]} rules Rule configurations
	     * @returns {Sincerity.Dependencies.Module[]}
	     */
	    Public.resolve = function(moduleSpecifications, repositories, rules) {
	    	// Create root modules
	    	var roots = []
	    	for (var m in moduleSpecifications) {
	    		var moduleSpecification = moduleSpecifications[m]
	    		var module = new Module.Module()
	    		module.specification = Module.createModuleSpecification(moduleSpecification, this.defaultType)
	    		roots.push(module)
	    	}

	    	// Create repositories
	    	repositories = Sincerity.Objects.clone(repositories)
	    	for (var r in repositories) {
	    		repositories[r] = Module.createRepository(repositories[r], this.defaultType)
	    	}

	    	// Adjust rules
	    	rules = Sincerity.Objects.clone(rules)
			for (var r in rules) {
				var rule = rules[r]
				rule.type = rule.type || this.defaultType
			}

			// Resolve roots
			var pool = new java.util.concurrent.ForkJoinPool()
			var tasks = []
	    	for (var m in roots) {
	    		var module = roots[m]
	    		var task = this.resolveModuleTask(module, repositories, rules, true)
	    		tasks.push(pool.submit(task))
	    	}
			for (var t in tasks) {
				tasks[t].join()
			}
			
			// Flatten
			var resolved = [], unresolved = []
			
			function addModuleOnce(module) {
				if (module.identifier) {
					// TODO: combine dependents
		    		Sincerity.Objects.pushUnique(resolved, module, function(module1, module2) {
		    			return module1.identifier.isEqual(module2.identifier)
		    		})
				}
				else {
					// TODO: unique
					unresolved.push(module)
				}
				
	    		for (var d in module.dependencies) {
	    			addModuleOnce(module.dependencies[d])
	    		}
			}
			
	    	for (var m in roots) {
	    		addModuleOnce(roots[m])
	    	}

	    	return {
	    		roots: roots,
	    		resolved: resolved,
	    		unresolved: unresolved
	    	}
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
				var command = null
				
				// Try repositories
				for (var rr in repositories) {
					var repository = repositories[rr]
					command = repository.applyModuleRule(module, rule)
    				if (command) {
    					break
    				}
    			}

				if (null === command) {
					// TODO: unsupported rule warning
				}
				else if (command == 'exclude') {
					return
				}
				else if (command == 'excludeDependencies') {
					recursive = false
				}
			}

    		if (Sincerity.Objects.exists(module.identifier)) {
    			// Already resolved
    		}
    		else if (Sincerity.Objects.exists(module.specification)) {
				// Check to see if we've already resolved it
    			for (var i = this.resolvedCache.iterator(); i.hasNext(); ) {
    				var resolvedModule = i.next()
    				if (module.specification.isEqual(resolvedModule.specification)) {
    					module.copyFrom(resolvedModule)
    					this.resolvedCacheHits.incrementAndGet()
    					break
    				}
    			}
    			
        		if (!Sincerity.Objects.exists(module.identifier)) {
	    			// Resolve
		    		for (var r in repositories) {
			    		var repository = repositories[r]
			    		var allowedModuleIdentifiers = repository.getAllowedModuleIdentifiers(module.specification)
			    		if (allowedModuleIdentifiers.length) {
				    		allowedModuleIdentifiers.sort(function(moduleIdentifier1, moduleIdentifier2) {
				    			// Reverse version order
				    			return moduleIdentifier2.compare(moduleIdentifier1)
				    		})
				    		
			    			var bestModule = repository.getModule(allowedModuleIdentifiers[0])
			    			module.copyFrom(bestModule)
			    			// Note: another thread may have already put a compatible module in the case, but it's no big deal!
			    			// Better to have higher concurrency while allowing for repeated work.
			    			this.resolvedCache.add(module)
			    			
			    			break
			    		}
			    	}
        		}
    		}

			if (recursive) {
				// Resolve dependencies recursively
				var inForkJoin = Sincerity.JVM.inForkJoin(), tasks = []
		    	for (d in module.dependencies) {
		    		var dependency = module.dependencies[d]
		    		if (inForkJoin) {
		    			// Fork
		    			tasks.push(this.resolveModuleTask(dependency, repositories, rules, true).fork())
		    		}
		    		else {
		    			// Do now
		    			this.resolveModule(dependency, repositories, rules, true)
		    		}
		    	}
				// Join
				for (var t in tasks) {
					tasks[t].join()
				}
			}
	    }

	    /**
	     *
	     * @param {Sincerity.Dependencies.Module} module
	     * @param {Sincerity.Dependencies.Repository[]} repositories
	     * @param {Object[]} rules
	     * @param {Boolean} [recursive=false]
	     * @returns {java.util.concurrent.RecursiveAction}
	     */
	    Public.resolveModuleTask = function(module, repositories, rules, recursive) {
    		var resolver = this
    		return Sincerity.JVM.task(function() {
    			resolver.resolveModule(module, repositories, rules, recursive)
    		}, 'recursiveAction')
	    }

	    return Public
	}(Public))

	return Public
}()


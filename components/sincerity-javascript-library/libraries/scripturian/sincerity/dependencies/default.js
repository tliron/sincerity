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
	 * @param {Object} repositoryConfig
	 * @param {String} [repositoryConfig.type=defaultType] The repository type
	 * @param {String} [defaultType] Optional default type to fallback to if not specified in config
	 * @returns {Sincerity.Dependency.Repository}
	 */
	Public.createRepository = function(repositoryConfig, defaultType) {
		var className = Sincerity.Objects.capitalize(repositoryConfig.type || defaultType)
		var clazz = Public[className].Repository
		return new clazz(repositoryConfig)
	}

	/**
	 * Creates a module specification instance.
	 * 
	 * @param {Object} moduleSpecificationConfig
	 * @param {String} [moduleSpecificationConfig=defaultType] The module specification type
	 * @param {String} [defaultType] Optional default type to fallback to if not specified in config
	 * @returns {Sincerity.Dependency.ModuleSpecification}
	 */
	Public.createModuleSpecification = function(moduleSpecificationConfig, defaultType) {
		var className = Sincerity.Objects.capitalize(moduleSpecificationConfig.type || defaultType)
		var clazz = Public[className].ModuleSpecification
		return new clazz(moduleSpecificationConfig)
	}

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
	    
	    Public.addDependent = function(module) {
	    	var found = false
	    	for (var d in this.dependents) {
	    		var dependent = this.dependents[d]
	    		if (module.identifier.isEqual(dependent.identifier)) {
	    			found = true
	    			break
	    		}
	    	}
	    	if (!found) {
	    		this.dependents.push(module)
	    	}
	    }

	    Public.addDependents = function(module) {
	    	for (var d in module.dependents) {
	    		this.addDependent(module.dependents[d])
	    	}
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
	 * Handles resolving and fetching of dependency tree.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.Resolver
	 * 
     * @param {Object[]} confis.modules Module specification configurations
     * @param {Object[]} config.repositories Repository configurations
     * @param {Object[]} config.rules Rule configurations
     * @param {String} [config.defaultType='maven'] The default type
	 */
	Public.Resolver = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Resolver */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(config) {
	    	this.defaultType = config.defaultType || 'maven'
	    	this.rootModules = this.createModules(config.modules)
	    	this.repositories = this.createRepositories(config.repositories)
	    	this.rules = Sincerity.Objects.clone(config.rules)
			for (var r in this.rules) {
				var rule = this.rules[r]
				rule.type = rule.type || this.defaultType
			}

	    	this.resolvedModules = []
	    	this.resolvedModulesLock = Sincerity.JVM.newLock()
	    	this.unresolvedModules = []
	    	this.unresolvedModulesLock = Sincerity.JVM.newLock()

	    	this.resolvedCacheHits = new java.util.concurrent.atomic.AtomicInteger()
	    }
	    
	    Public.createModules = function(moduleSpecificationConfigs) {
	    	var modules = []
	    	for (var m in moduleSpecificationConfigs) {
	    		var moduleSpecificationConfig = moduleSpecificationConfigs[m]
	    		var module = new Module.Module()
	    		module.specification = Module.createModuleSpecification(moduleSpecificationConfig, this.defaultType)
	    		modules.push(module)
	    	}
	    	return modules
	    }
	    
	    Public.createRepositories = function(repositoryConfigs) {
	    	var repositories = []
	    	for (var r in repositoryConfigs) {
	    		var repositoryConfig = repositoryConfigs[r]
	    		var repository = Module.createRepository(repositoryConfig, this.defaultType)
	    		repositories.push(repository)
	    	}
	    	return repositories
	    }
	    
	    Public.getResolvedModule = function(moduleSpecification) {
			return Sincerity.JVM.withLock(this.resolvedModulesLock, function() {
    			for (var m in this.resolvedModules) {
    				var module = this.resolvedModules[m]
    				if (moduleSpecification.isEqual(module.specification)) {
    					//println('!!!! cache hit: ' + module.specification.toString())
    					this.resolvedCacheHits.incrementAndGet()
    					return module
    				}
    			}
			}, this)
	    }

	    Public.addResolvedModule = function(module) {
			Sincerity.JVM.withLock(this.resolvedModulesLock, function() {
				var found = false
    			for (var m in this.resolvedModules) {
    				var resolvedModule = this.resolvedModules[m]
    				if (module.identifier.isEqual(resolvedModule.identifier)) {
    					resolvedModule.addDependents(module)
    					found = true
    					break
    				}
    			}
				if (!found) {
					this.resolvedModules.push(module)
				}
			}, this)
	    }

	    Public.addUnresolvedModule = function(module) {
			Sincerity.JVM.withLock(this.unresolvedModulesLock, function() {
				var found = false
    			for (var m in this.unresolvedModules) {
    				var unresolvedModule = this.unresolvedModules[m]
    				if (module.specification.isEqual(unresolvedModule.specification)) {
    					unresolvedModule.addDependents(module)
    					found = true
    					break
    				}
    			}
				if (!found) {
					this.unresolvedModules.push(module)
				}
			}, this)
	    }
	    
	    Public.addModule = function(module) {
    		if (Sincerity.Objects.exists(module.identifier)) {
    			this.addResolvedModule(module)
    		}
    		else {
    			this.addUnresolvedModule(module)
    		}
	    }

	    Public.resolve = function() {
			// Resolve roots
			var pool = new java.util.concurrent.ForkJoinPool(10)
	    	try {
				var tasks = []
		    	for (var m in this.rootModules) {
		    		var module = this.rootModules[m]
		    		var task = this.resolveModuleTask(module, this.repositories, this.rules, true)
		    		tasks.push(pool.submit(task))
		    	}
				for (var t in tasks) {
					tasks[t].join()
				}
	    	}
	    	finally {
	    		pool.shutdown()
	    	}
	    	
	    	this.resolvedModules.sort(function(module1, module2) {
	    		return module1.identifier.toString().localeCompare(module2.identifier.toString())
	    	})
	    	this.unresolvedModules.sort(function(module1, module2) {
	    		return module1.specification.toString().localeCompare(module2.specification.toString())
	    	})
	    }
	    
	    /**
	     *
	     * @param {Sincerity.Dependencies.Module} module
	     * @param {Sincerity.Dependencies.Repository[]} repositories
	     * @param {Object[]} rules
	     * @param {Boolean} [recursive=false]
	     */
	    Public.resolveModule = function(module, repositories, rules, recursive) {
	    	var exclude = false
	    	
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
					exclude = true
				}
				else if (command == 'excludeDependencies') {
					recursive = false
				}
			}

    		if (Sincerity.Objects.exists(module.identifier)) {
    			// Already resolved
    		}
    		else if (Sincerity.Objects.exists(module.specification)) {
    			if (!exclude) {
					// Check to see if we've already resolved it
	    			var resolvedModule = this.getResolvedModule(module.specification)
	    			if (resolvedModule) {
						module.copyFrom(resolvedModule)
	    			}
	    			else {
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
				    			break
				    		}
				    	}
	        		}
    			}
    		}

			this.addModule(module)

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
			else {
		    	for (d in module.dependencies) {
		    		this.addModule(module.dependencies[d])
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


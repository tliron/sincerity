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
	 * @param {String} [repositoryConfig.platform=defaultPlatform] The repository platform
	 * @param {String} [defaultPlatform] Optional default platform to fallback to if not specified in config
	 * @returns {Sincerity.Dependency.Repository}
	 */
	Public.createRepository = function(repositoryConfig, defaultPlatform) {
		var className = Sincerity.Objects.capitalize(repositoryConfig.platform || defaultPlatform)
		var clazz = Public[className].Repository
		return new clazz(repositoryConfig)
	}

	/**
	 * Creates a module specification instance.
	 * 
	 * @param {Object} moduleSpecificationConfig
	 * @param {String} [moduleSpecificationConfig=defaultPlatform] The module specification platform
	 * @param {String} [defaultPlatform] Optional default platform to fallback to if not specified in config
	 * @returns {Sincerity.Dependency.ModuleSpecification}
	 */
	Public.createModuleSpecification = function(moduleSpecificationConfig, defaultPlatform) {
		var className = Sincerity.Objects.capitalize(moduleSpecificationConfig.platform || defaultPlatform)
		var clazz = Public[className].ModuleSpecification
		return new clazz(moduleSpecificationConfig)
	}

	/**
	 * A module can have dependencies as well as reasons.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.Module
	 */
	Public.Module = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Module */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function() {
	    	this.explicit = false
	    	this.identifier = null
	    	this.repository = null
	    	this.specification = null
	    	this.dependencies = []
	    	this.reasons = []
	    }
	    
	    /**
	     * Copies identifier, repository, and dependencies from another module.
	     * 
	     * @param {Sincerity.Dependencies.Module} module
	     */
	    Public.copyResolutionFrom = function(module) {
	    	this.identifier = module.identifier.clone()
	    	this.repository = module.repository
	    	this.dependencies = []
	    	for (var m in module.dependencies) {
	    		this.dependencies.push(module.dependencies[m])
	    	}
	    }

	    /**
	     * Adds a new reason if we don't have it already.
	     * 
	     * @param {Sincerity.Dependencies.Module} module
	     */
	    Public.addReason = function(module) {
	    	var found = false
	    	for (var m in this.reasons) {
	    		var reasonModule = this.reasons[m]
	    		if (reasonModule.identifier.compare(module.identifier) === 0) {
	    			found = true
	    			break
	    		}
	    	}
	    	if (!found) {
	    		this.reasons.push(module)
	    	}
	    }

	    /**
	     * Removes a reason if we have it.
	     * 
	     * @param {Sincerity.Dependencies.Module} module
	     */
	    Public.removeReason = function(module) {
	    	for (var m in this.reasons) {
	    		var reasonModule = this.reasons[m]
	    		if (reasonModule.identifier.compare(module.identifier) === 0) {
	    			this.reasons.splice(m, 1)
	    			break
	    		}
	    	}
	    }

	    /**
	     * Adds all reasons of another module, and makes us explicit if the other module is explicit.
	     * 
	     * @param {Sincerity.Dependencies.Module} module
	     * @see Sincerity.Dependencies.Module#addReason
	     */
	    Public.merge = function(module) {
			if (module.explicit) {
				this.explicit = true
			}
	    	for (var m in module.reasons) {
	    		this.addReason(module.reasons[m])
	    	}
	    }
	    
	    Public.replaceModule = function(oldModule, newModule, recursive) {
	    	this.removeReason(oldModule)
	    	for (var m in this.dependencies) {
	    		var module = this.dependencies[m]
	    		if (module.identifier && (module.identifier.compare(oldModule.identifier) === 0)) {
	    			module = this.dependencies[d] = newModule
	    			module.addReason(this)
	    		}
	    		if (recursive) {
	    			module.replaceModule(oldModule, newModule, true)
	    		}
	    	}
	    }

	    /**
	     * Represent the instance as a string.
	     * 
	     * @returns {String} A string representation
	     */
	    Public.toString = function() {
	    	var r = '', prefix = ''
	    	prefix += this.explicit ? '*' : '+' // explicit?
	    	prefix += Sincerity.Objects.exists(this.identifier) ? '!' : '?' // resolved?
	    	if (Sincerity.Objects.exists(this.identifier)) {
	    		r += 'id=' + this.identifier.toString()
	    	}
	    	if (Sincerity.Objects.exists(this.specification)) {
	    		if (r.length) { r += ', ' }
	    		r += 'spec=' + this.specification.toString()
	    	}
	    	if (this.dependencies.length) {
	    		if (r.length) { r += ', ' }
	    		r += 'dependencies=' + this.dependencies.length
	    	}
	    	if (this.reasons.length) {
	    		if (r.length) { r += ', ' }
	    		r += 'reasons=' + this.reasons.length
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
		    	for (var m in this.dependencies) {
		    		var module = this.dependencies[m]
		    		module.dump(out, true, indent + 1)
		    	}
	    	}
	    }

	    return Public
	}(Public))

	/**
	 * Base class for module identifiers.
	 * <p>
	 * These are implemented differently per platform.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.ModuleIdentifier
	 */
	Public.ModuleIdentifier = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.ModuleIdentifier */
	    var Public = {}
	    
		/**
		 * Compare to another module identifier in terms of newness.
		 * 
	     * @param {Sincerity.Dependencies.ModuleIdentifier} moduleIdentifier The module identifier to compare to us
		 * @returns {Number} NaN if incompatible, -1 if moduleIdentifier is newer, 0 if equal, 1 if we are newer
		 */
		Public.compare = function(moduleIdentifier) {
			return NaN
		}

		/**
		 * Creates a copy of this instance.
		 * 
		 * @returns {Sincerity.Dependencies.ModuleIdentifier}
		 */
		Public.clone = function() {
			return new Sincerity.Dependencies.ModuleIdentifier()
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
	 * These are implemented differently per platform.
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
		 * Creates a copy of this instance.
		 * 
		 * @returns {Sincerity.Dependencies.ModuleSpecification}
		 */
		Public.clone = function() {
			return new Sincerity.Dependencies.ModuleSpecification()
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

	    Public.fetchModule = function(moduleIdentifier, directory, overwrite, eventHandler) {
	    }
	    
	    Public.applyModuleRule = function(module, rule) {
	    	return false
	    }

		/**
		 * Creates a copy of this instance.
		 * 
		 * @returns {Sincerity.Dependencies.Repository}
		 */
		Public.clone = function() {
			return new Sincerity.Dependencies.ModuleSpecification()
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
     * @param {String} [config.defaultPlatform='maven'] The default platform to use if unspecified
	 */
	Public.Resolver = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Resolver */
	    var Public = {}
	    
	    /** @ignore */
	    Public._construct = function(config) {
	    	this.defaultPlatform = config.defaultPlatform || 'maven'
	    	this.conflictPolicy = config.conflictPolicy || 'newest'
	    	
	    	this.explicitModules = this.createExplicitModules(config.modules)
	    	this.repositories = this.createRepositories(config.repositories)
	    	
	    	this.rules = Sincerity.Objects.clone(config.rules)
			for (var r in this.rules) {
				var rule = this.rules[r]
				rule.platform = rule.platform || this.defaultPlatform
			}

	    	this.resolvedModules = []
	    	this.resolvedModulesLock = Sincerity.JVM.newLock()
	    	this.unresolvedModules = []
	    	this.unresolvedModulesLock = Sincerity.JVM.newLock()
	    	this.conflicts = []

	    	this.resolvedCacheHits = new java.util.concurrent.atomic.AtomicInteger()
	    	
	    	this.eventHandler = new Sincerity.Dependencies.EventHandlers()
	    }
	    
	    /**
	     * Creates module instances based on module specification configs.
	     * <p>
	     * If the platform is not specified in the config, it will be defaultPlatform.
	     * 
	     * @param {Object[]} moduleSpecificationConfigs
	     * @returns {Sincerity.Dependencies.Module[]}
	     */
	    Public.createExplicitModules = function(moduleSpecificationConfigs) {
	    	var modules = []
	    	for (var m in moduleSpecificationConfigs) {
	    		var moduleSpecificationConfig = moduleSpecificationConfigs[m]
	    		var module = new Module.Module()
	    		module.explicit = true
	    		module.specification = Module.createModuleSpecification(moduleSpecificationConfig, this.defaultPlatform)
	    		modules.push(module)
	    	}
	    	return modules
	    }
	    
	    /**
	     * Creates repository instances based on configs.
	     * <p>
	     * If the platform is not specified in the config, it will be defaultPlatform.
	     * 
	     * @param {Object[]} repositoryConfigs
	     * @returns {Sincerity.Dependencies.Repository[]}
	     */
	    Public.createRepositories = function(repositoryConfigs) {
	    	var repositories = []
	    	for (var r in repositoryConfigs) {
	    		var repositoryConfig = repositoryConfigs[r]
	    		var repository = Module.createRepository(repositoryConfig, this.defaultPlatform)
	    		repositories.push(repository)
	    	}
	    	return repositories
	    }
	    
	    /**
	     * Gets an instance of a module (from resolvedModules) if it has already been resolved.
	     *
	     * @param {Sincerity.Dependencies.ModuleSpecification}
	     * @returns {Sincerity.Dependencies.Module[]} or null if not yet resolved
	     */
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
    			return null
			}, this)
	    }

	    Public.addResolvedModule = function(module) {
			Sincerity.JVM.withLock(this.resolvedModulesLock, function() {
				var found = false
    			for (var m in this.resolvedModules) {
    				var resolvedModule = this.resolvedModules[m]
    				if (module.identifier.compare(resolvedModule.identifier) === 0) {
    					// Merge
    					resolvedModule.merge(module)
    					found = true
    					break
    				}
    			}
				if (!found) {
					this.resolvedModules.push(module)
				}
			}, this)
	    }

	    Public.removeResolvedModule = function(module) {
			Sincerity.JVM.withLock(this.resolvedModulesLock, function() {
				var found = null
    			for (var m in this.resolvedModules) {
    				var resolvedModule = this.resolvedModules[m]
    				if (module.identifier.compare(resolvedModule.identifier) === 0) {
    					found = m
    					break
    				}
    			}
				if (found !== null) {
					this.resolvedModules.splice(found, 1)
				}
			}, this)
	    }

	    Public.addUnresolvedModule = function(module) {
			Sincerity.JVM.withLock(this.unresolvedModulesLock, function() {
				var found = false
    			for (var m in this.unresolvedModules) {
    				var unresolvedModule = this.unresolvedModules[m]
    				if (module.specification.isEqual(unresolvedModule.specification)) {
    					// Merge
    					unresolvedModule.merge(module)
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
	    
	    Public.replaceModule = function(oldModule, newModule) {
	    	for (var m in this.explicitModules) {
	    		var module = this.explicitModules[m]
	    		if (module.identifier && (module.identifier.compare(oldModule.identifier) === 0)) {
	    			module = this.explicitModules[m] = newModule
	    			module.explicit = true
	    		}
    			module.replaceModule(oldModule, newModule, true)
	    	}
	    }

	    /**
	     * Goes over the explicitModules and resolves them recursively.
	     * This is done using fork/join parallelism for better efficiency.
	     * <p>
	     * When finished, resolvedModules and unresolvedModules would be filled
	     * appropriately. 
	     * 
	     * @see Sincerity.Dependencies.Resolver#resolveModule
	     */
	    Public.resolve = function() {
			this.eventHandler.handleEvent({message: 'Resolving...'})

			// Resolve explicit modules
			var pool = new java.util.concurrent.ForkJoinPool(10)
	    	try {
				var tasks = []
		    	for (var m in this.explicitModules) {
		    		var module = this.explicitModules[m]
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
	    	
	    	// Sort resolved modules
	    	this.resolvedModules.sort(function(module1, module2) {
	    		return module1.identifier.toString().localeCompare(module2.identifier.toString())
	    	})
	    	
	    	// Sort unresolved modules
	    	this.unresolvedModules.sort(function(module1, module2) {
	    		return module1.specification.toString().localeCompare(module2.specification.toString())
	    	})
	    	
	    	// Find conflicts
	    	var potentialConflicts = []
	    	for (var m in this.resolvedModules) {
	    		potentialConflicts.push(this.resolvedModules[m])
	    	}
	    	
	    	var module
	    	while (module = potentialConflicts.pop()) {
	    		var conflicts = [module]
		    	for (var m in potentialConflicts) {
		    		var otherModule = potentialConflicts[m]
		    		var comparison = module.identifier.compare(otherModule.identifier)
		    		if ((comparison === -1) || (comparison === 1)) {
		    			conflicts.push(otherModule)
		    		}
		    	}
	    		if (conflicts.length > 1) {
	    			this.conflicts.push(conflicts)
	    		}
	    	}
	    	
	    	// Sort conflicts
	    	for (var c in this.conflicts) {
	    		var conflicts = this.conflicts[c]
	    		conflicts.sort(function(module1, module2) {
	    			// Reverse newness order
	    			return module2.identifier.compare(module1.identifier)
	    		})
	    	}
	    	
	    	// Resolve conflicts
	    	for (var c in this.conflicts) {
	    		var conflicts = []
	    		for (var m in this.conflicts[c]) {
	    			conflicts.push(this.conflicts[c][m])
	    		}
	    		
	    		// Choose a module
	    		var chosenModuleIndex = null
	    		if (this.conflictPolicy == 'newest') {
	    			chosenModuleIndex = 0
	    		}
	    		else if (this.conflictPolicy == 'oldest') {
	    			chosenModuleIndex = conflicts.length - 1
	    		}
	    		else {
	    			// TODO
	    			continue
	    		}
	    		
	    		var chosenModule = conflicts[chosenModuleIndex]
	    		conflicts.splice(chosenModuleIndex, 1)

				this.eventHandler.handleEvent({message: 'Resolved conflict: ' + chosenModule.identifier.toString()})

	    		// Merge all reasons into chosen module, and remove non-chosen modules from resolvedModules
	    		for (var m in conflicts) {
	    			var module = conflicts[m]
	    			chosenModule.merge(module)
	    			this.removeResolvedModule(module)
	    			this.replaceModule(module, chosenModule)
	    		}
	    	}
	    }
	    
	    /**
	     * Fetches the resolved modules.
	     */
	    Public.fetch = function(directory, overwrite) {
	    	for (var m in this.resolvedModules) {
	    		var module = this.resolvedModules[m]
	    		module.repository.fetchModule(module.identifier, directory, overwrite, this.eventHandler)
	    	}
	    }
	    
	    /**
	     * Resolves a module, optionally resolving its dependencies recursively (using fork/join
	     * parallelism).
	     * <p>
	     * "Resolving" means finding the best identifier available from all the repositories
	     * that matches the specification. A successful resolution means that the module has
	     * an identifier. An unresolved module has only a specification, but no identifier.
	     * <p>
	     * A cache of resolved modules is maintained in the resolver to avoid resolving
	     * the same module twice.
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
					this.eventHandler.handleEvent({message: 'Excluding: ' + module.specification.toString()})
					exclude = true
				}
				else if (command == 'excludeDependencies') {
					this.eventHandler.handleEvent({message: 'Excluding dependencies: ' + module.specification.toString()})
					recursive = false
				}
			}

    		if (Sincerity.Objects.exists(module.identifier)) {
    			// Already resolved
    		}
    		else if (!exclude && Sincerity.Objects.exists(module.specification)) {
				// Check to see if we've already resolved it
    			var resolvedModule = this.getResolvedModule(module.specification)
    			if (!resolvedModule) {
	    			// Gather allowed module identifiers from all repositories
					this.eventHandler.handleEvent({message: 'Resolving: ' + module.specification.toString() + '...'})
					
    				var moduleIdentifiers = []
		    		for (var r in repositories) {
			    		var repository = repositories[r]
			    		var allowedModuleIdentifiers = repository.getAllowedModuleIdentifiers(module.specification)
			    		
			    		// Note: the first repository to report an identifier will "win," the following repositories will have their reports discarded
			    		moduleIdentifiers = Sincerity.Objects.concatUnique(moduleIdentifiers, allowedModuleIdentifiers, function(moduleIdentifier1, moduleIdentifier2) {
			    			return moduleIdentifier1.compare(moduleIdentifier2) === 0
			    		})
			    	}

    				// Pick the best module identifier
		    		if (moduleIdentifiers.length > 0) {
		    			moduleIdentifiers.sort(function(moduleIdentifier1, moduleIdentifier2) {
			    			// Reverse newness order
			    			return moduleIdentifier2.compare(moduleIdentifier1)
			    		})
			    		
			    		// Best module is first (newest)
		    			resolvedModule = repository.getModule(moduleIdentifiers[0])

						this.eventHandler.handleEvent({message: '-> ' + resolvedModule.identifier.toString()})
		    		}
		    		else {
						this.eventHandler.handleEvent({message: ':('})
		    		}
    			}

    			if (resolvedModule) {
					module.copyResolutionFrom(resolvedModule)
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
				// Add dependencies as is (unresolved)
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

	/**
	 * Handles events.
	 * <p>
	 * This class can also be used as a null event handler, because it does nothing.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.EventHandler
	 */
	Public.EventHandler = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.EventHandler */
	    var Public = {}
	    
	    /**
	     * @param {Object} event
	     * @returns {Boolean} true if the event was swallowed by the handler
	     */
	    Public.handleEvent = function(event) {
	    	return false
	    }

	    return Public
	}(Public))

	/**
	 * A handler that delegates to other handlers.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.EventHandlers
	 */
	Public.EventHandlers = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.EventHandlers */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.EventHandler

	    /** @ignore */
	    Public._construct = function() {
	    	this.eventHandlers = Sincerity.JVM.newList(true)
	    }
	    
	    Public.handleEvent = function(event) {
	    	for (var i = this.eventHandlers.iterator(); i.hasNext(); ) {
	    		var eventHandler = i.next()
	    		if (eventHandler.handleEvent(event) === true) {
	    			return true
	    		}
	    	}
	    	return false
	    }
	    
	    Public.add = function(eventHandler) {
	    	this.eventHandlers.add(eventHandler)
	    }

	    return Public
	}(Public))

	/**
	 * A handler that outputs events to the console.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.ConsoleEventHandler
	 */
	Public.ConsoleEventHandler = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.ConsoleEventHandler */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.EventHandler

	    /** @ignore */
	    Public._construct = function(out) {
	    	this.out = out
	    }

	    Public.handleEvent = function(event) {
	    	this.out.println(event.message)
	    	return false
	    }

	    return Public
	}(Public))

	/**
	 * A handler that outputs events to a log.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.LogEventHandler
	 */
	Public.LogEventHandler = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.LogEventHandler */
	    var Public = {}

	    /** @ignore */
	    Public._inherit = Module.EventHandler
	    
	    Public.handleEvent = function(event) {
	    	return false
	    }

	    return Public
	}(Public))

	return Public
}()


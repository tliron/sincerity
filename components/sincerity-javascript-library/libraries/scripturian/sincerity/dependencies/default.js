//
// This file is part of the Sincerity Foundation Library
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

	Public.registerHooks = function(out) {
		java.lang.Thread.defaultUncaughtExceptionHandler = function(thread, throwable) {
			this.println('Uncaught exception: ' + throwable)
		}.toUncaughtExceptionHandler(out)
	
		Sincerity.JVM.addShutdownHook(function() {
	    	this.println('Shutting down... ')
	    	
	    	// Cleanly shutdown all pools
			for (var p in pools) {
				pools[p].shutdownNow()
			}
	    	
	    	// Interrupt all non-daemon threads
			for (var i = java.lang.Thread.allStackTraces.keySet().iterator(); i.hasNext(); ) {
				var thread = i.next()
				if (!thread.daemon) {
					//this.println(thread)
					var trace = thread.stackTrace
					for (var t in trace) {
						//this.println('  ' + trace[t])
					}
					//thread.interrupt()
				}
			}
			
	    	this.println('done!')
	    	java.lang.Runtime.runtime.halt(0)
		}, 'Sincerity.Dependencies Shutdown Hook', out)
	}

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
	 * A module can have dependencies as well as supplicants.
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
	    	this.specification = null
	    	this.dependencies = []
	    	this.supplicants = []
	    }
	    
	    /**
	     * Copies identifier, repository, and dependencies from another module.
	     * 
	     * @param {Sincerity.Dependencies.Module} module
	     */
	    Public.copyResolutionFrom = function(module) {
	    	this.identifier = module.identifier.clone()
	    	this.dependencies = []
	    	for (var m in module.dependencies) {
	    		this.dependencies.push(module.dependencies[m])
	    	}
	    }

	    /**
	     * Adds a new supplicant if we don't have it already.
	     * 
	     * @param {Sincerity.Dependencies.Module} module
	     */
	    Public.addSupplicant = function(module) {
	    	var found = false
	    	for (var m in this.supplicants) {
	    		var supplicantModule = this.supplicants[m]
	    		if (supplicantModule.identifier.compare(module.identifier) === 0) {
	    			found = true
	    			break
	    		}
	    	}
	    	if (!found) {
	    		this.supplicants.push(module)
	    	}
	    }

	    /**
	     * Removes a supplicant if we have it.
	     * 
	     * @param {Sincerity.Dependencies.Module} module
	     */
	    Public.removeSupplicant = function(module) {
	    	for (var m in this.supplicants) {
	    		var supplicantModule = this.supplicants[m]
	    		if (supplicantModule.identifier.compare(module.identifier) === 0) {
	    			this.supplicants.splice(m, 1)
	    			break
	    		}
	    	}
	    }

	    /**
	     * Adds all supplicants of another module, and makes us explicit if the other module is explicit.
	     * 
	     * @param {Sincerity.Dependencies.Module} module
	     * @see Sincerity.Dependencies.Module#addSupplicant
	     */
	    Public.merge = function(module) {
			if (module.explicit) {
				this.explicit = true
			}
	    	for (var m in module.supplicants) {
	    		this.addSupplicant(module.supplicants[m])
	    	}
	    }
	    
	    Public.replaceModule = function(oldModule, newModule, recursive) {
	    	this.removeSupplicant(oldModule)
	    	for (var m in this.dependencies) {
	    		var module = this.dependencies[m]
	    		if (module.identifier && (module.identifier.compare(oldModule.identifier) === 0)) {
	    			module = this.dependencies[m] = newModule
	    			module.addSupplicant(this)
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
	    Public.toString = function(long) {
	    	var r = '', prefix = ''
	    	if (Sincerity.Objects.exists(this.identifier)) {
	    		r += 'id=' + this.identifier.toString()
	    	}
	    	if ((long || !(Sincerity.Objects.exists(this.identifier))) && Sincerity.Objects.exists(this.specification)) {
	    		if (r.length) { r += ', ' }
	    		r += 'spec=' + this.specification.toString()
	    	}
	    	if (long) {
	    		prefix += this.explicit ? '*' : '+' // explicit?
	    		prefix += Sincerity.Objects.exists(this.identifier) ? '!' : '?' // identified?
		    	if (this.dependencies.length) {
		    		if (r.length) { r += ', ' }
		    		r += 'dependencies=' + this.dependencies.length
		    	}
		    	if (this.supplicants.length) {
		    		if (r.length) { r += ', ' }
		    		r += 'supplicants=' + this.supplicants.length
		    	}
	    	}
	    	if (prefix.length) {
	    		r = prefix + ' ' + r
	    	}
	    	return r
	    }
	    
	    Public.dump = function(out, withDependencies, indent) {
	    	printTree(out, this, function(module) { return module.toString(!withDependencies) }, withDependencies ? function(module) { return module.dependencies } : null, indent)
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
	    
	    /** @ignore */
	    Public._construct = function(config) {
	    	config = config || {}
	    	this.id = config.id || 0
	    	this.all = Sincerity.Objects.ensure(config.all, true)
	    	var parallelism = config.parallelism || 5
	    	this.executor = newExecutor(parallelism)
	    }
	    
	    Public.hasModule = function(moduleIdentifier) {
	    	return false
	    }

	    Public.getModule = function(moduleIdentifier, notifier) {
	    	return null
	    }

	    Public.getAllowedModuleIdentifiers = function(moduleSpecification, notifier) {
	    	return []
	    }

	    Public.installModule = function(moduleIdentifier, directory, overwrite, notifier) {
	    }

	    Public.installModuleFuture = function(moduleIdentifier, directory, overwrite, notifier) {
    		var task = function() {
    			try {
    				this.installModule(moduleIdentifier, directory, overwrite, notifier)
    			}
    			catch (x) {
    				notifier.notify({type: 'error', message: 'Install error for ' + moduleIdentifier.toString() + ': ' + x.message, exception: x})
    			}
    		}.toTask(null, this)
    		return this.executor.submit(task)
	    }

	    Public.applyModuleRule = function(module, rule, notifier) {
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
	    	return 'id=' + this.id
	    }

	    return Public
	}(Public))
	
	/**
	 * Handles identifying and installing dependencies.
	 * 
	 * @class
	 * @name Sincerity.Dependencies.Manager
	 * 
     * @param {Object[]} confis.modules Module specification configurations
     * @param {Object[]} config.repositories Repository configurations
     * @param {Object[]} config.rules Rule configurations
     * @param {String} [config.defaultPlatform='maven'] The default platform to use if unspecified
	 */
	Public.Manager = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Sincerity.Dependencies.Manager */
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

	    	this.identifiedModules = []
	    	this.identifiedModulesLock = Sincerity.JVM.newLock()
	    	this.unidentifiedModules = []
	    	this.unidentifiedModulesLock = Sincerity.JVM.newLock()
	    	this.conflicts = []

	    	this.identifiedCacheHits = new java.util.concurrent.atomic.AtomicInteger()
	    	
	    	this.eventHandler = new Sincerity.Dependencies.EventHandlers()
	    	
	    	this.forkJoinPool = newForkJoinPool(10)
	    }
	    
	    Public.notify = function(event) {
	    	if (Sincerity.Objects.isString(event)) {
	    		event = {message: event}
	    	}
	    	this.eventHandler.handleEvent(event)
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
	    		repositoryConfig.id = repositoryConfig.id || r
	    		var repository = Module.createRepository(repositoryConfig, this.defaultPlatform)
	    		repositories.push(repository)
	    	}
	    	return repositories
	    }
	    
	    /**
	     * Gets an instance of a module (from identifiedModules) if it has already been identified.
	     *
	     * @param {Sincerity.Dependencies.ModuleSpecification}
	     * @returns {Sincerity.Dependencies.Module[]} or null if not yet identified
	     */
	    Public.getIdentifiedModule = function(moduleSpecification) {
			for (var m in this.identifiedModules) {
				var module = this.identifiedModules[m]
				if (moduleSpecification.isEqual(module.specification)) {
					//println('!!!! cache hit: ' + module.specification.toString())
					this.identifiedCacheHits.incrementAndGet()
					return module
				}
			}
			return null
	    }.withLock('identifiedModulesLock')
	    
	    Public.addIdentifiedModule = function(module) {
			var found = false
			for (var m in this.identifiedModules) {
				var identifiedModule = this.identifiedModules[m]
				if (module.identifier.compare(identifiedModule.identifier) === 0) {
					// Merge
					identifiedModule.merge(module)
					found = true
					break
				}
			}
			if (!found) {
				this.identifiedModules.push(module)
			}
	    }.withLock('identifiedModulesLock')

	    Public.removeIdentifiedModule = function(module) {
			var found = null
			for (var m in this.identifiedModules) {
				var identifiedModule = this.identifiedModules[m]
				if (module.identifier.compare(identifiedModule.identifier) === 0) {
					found = m
					break
				}
			}
			if (found !== null) {
				this.identifiedModules.splice(found, 1)
			}
	    }.withLock('identifiedModulesLock')

	    Public.addUnidentifiedModule = function(module) {
			var found = false
			for (var m in this.unidentifiedModules) {
				var unidentifiedModule = this.unidentifiedModules[m]
				if (module.specification.isEqual(unidentifiedModule.specification)) {
					// Merge
					unidentifiedModule.merge(module)
					found = true
					break
				}
			}
			if (!found) {
				this.unidentifiedModules.push(module)
			}
	    }.withLock('unidentifiedModulesLock')
	    
	    Public.addModule = function(module) {
    		if (Sincerity.Objects.exists(module.identifier)) {
    			this.addIdentifiedModule(module)
    		}
    		else {
    			this.addUnidentifiedModule(module)
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
	     * Goes over explicitModules and identifies them recursively.
	     * This is done using fork/join parallelism for better efficiency.
	     * <p>
	     * When finished, identifiedModules and unidentifiedModules would be filled
	     * appropriately. 
	     * 
	     * @see Sincerity.Dependencies.Manager#identifyModule
	     */
	    Public.identify = function() {
	    	var id = Sincerity.Objects.uniqueString()
			this.notify({type: 'begin', id: id, message: 'Identifying'})
			
			// Identify explicit modules
			var tasks = []
	    	for (var m in this.explicitModules) {
	    		var module = this.explicitModules[m]
	    		tasks.push(this.identifyModuleTask(module, true))
	    	}
			this.forkJoinPool.invokeAll(Sincerity.JVM.toList(tasks))
	    	
	    	// Sort identified modules
	    	this.identifiedModules.sort(function(module1, module2) {
	    		return module1.identifier.toString().localeCompare(module2.identifier.toString())
	    	})
	    	
	    	// Sort unidentified modules
	    	this.unidentifiedModules.sort(function(module1, module2) {
	    		return module1.specification.toString().localeCompare(module2.specification.toString())
	    	})
	    	
	    	var count = this.identifiedModules.length

	    	this.resolveConflicts()

	    	this.notify({type: 'end', id: id, message: 'Made ' + count + ' identifications'})
	    }
	    
	    /**
	     * Identifies a module, optionally identifying its dependencies recursively (supporting fork/join
	     * parallelism).
	     * <p>
	     * "Identification" means finding the best identifier available from all the candidates in all
	     * the repositories that match the specification. A successful identification results in the the
	     * module has an identifier. An unidentified module has only a specification, but no identifier.
	     * <p>
	     * A cache of identified modules is maintained in the manager to avoid identifying
	     * the same module twice.
	     *
	     * @param {Sincerity.Dependencies.Module} module
	     * @param {Boolean} [recursive=false]
	     */
	    Public.identifyModule = function(module, recursive) {
	    	var context = {
	    		repositories: [],
	    		exclude: false,
	    		recursive: recursive
	    	}
	    	
	    	for (var r in this.repositories) {
	    		var repository = this.repositories[r]
	    		if (repository.all) {
	    			context.repositories.push(repository)
	    		}
	    	}
	    	
	    	this.applyRules(module, context)

    		if (Sincerity.Objects.exists(module.identifier)) {
    			// Already identified
    		}
    		else if (!context.exclude && Sincerity.Objects.exists(module.specification)) {
				// Check to see if we've already identified it
    			var identifiedModule = this.getIdentifiedModule(module.specification)
    			if (!identifiedModule) {
    				var id = Sincerity.Objects.uniqueString()
    				
	    			// Gather allowed module identifiers from all repositories
					this.notify({type: 'begin', id: id, message: 'Identifying ' + module.specification.toString()})
					
    				var moduleIdentifiers = []
		    		for (var r in context.repositories) {
			    		var repository = context.repositories[r]
			    		var allowedModuleIdentifiers = repository.getAllowedModuleIdentifiers(module.specification, this)
			    		
			    		// Note: the first repository to report an identifier will "win," the following repositories will have their reports discarded
			    		moduleIdentifiers = Sincerity.Objects.concatUnique(moduleIdentifiers, allowedModuleIdentifiers, function(moduleIdentifier1, moduleIdentifier2) {
			    			return moduleIdentifier1.compare(moduleIdentifier2) === 0
			    		})
			    	}

    				// Pick the best module identifier
		    		if (moduleIdentifiers.length) {
		    			moduleIdentifiers.sort(function(moduleIdentifier1, moduleIdentifier2) {
			    			// Reverse newness order
			    			return moduleIdentifier2.compare(moduleIdentifier1)
			    		})
			    		
			    		// Best module is first (newest)
			    		var identifiedModuleIdentifier = moduleIdentifiers[0]
		    			identifiedModule = identifiedModuleIdentifier.repository.getModule(identifiedModuleIdentifier, this)

		    			if (identifiedModule) {
		    				this.notify({type: 'end', id: id, message: 'Identified ' + identifiedModule.identifier.toString() + ' in ' + identifiedModule.identifier.repository.id + ' repository'})
		    			}
		    			else {
							this.notify({type: 'fail', id: id, message: 'Could not get module ' + identifiedModuleIdentifier.toString() + ' from ' + identifiedModuleIdentifier.repository.id + ' repository'})
		    			}
		    		}
		    		else {
						this.notify({type: 'fail', id: id, message: 'Could not identify ' + module.specification.toString()})
		    		}
    			}

    			if (identifiedModule) {
					module.copyResolutionFrom(identifiedModule)
    			}
    		}

	    	if (!context.exclude) {
	    		this.addModule(module)
	    	}

			if (context.recursive) {
				// Identify dependencies recursively
				var pool = java.util.concurrent.ForkJoinTask.pool
				var tasks = []
		    	for (d in module.dependencies) {
		    		var dependency = module.dependencies[d]
		    		if (null !== pool) {
		    			// Fork
		    			tasks.push(this.identifyModuleTask(dependency, true))
		    		}
		    		else {
		    			// Do now
		    			this.identifyModule(dependency, true)
		    		}
		    	}
	    		if (tasks.length) {
	    			pool.invokeAll(Sincerity.JVM.toList(tasks))
	    		}
			}
			else {
				// Add dependencies as is (unidentified)
		    	for (d in module.dependencies) {
		    		this.addModule(module.dependencies[d])
		    	}				
			}
	    }

	    /**
	     *
	     * @param {Sincerity.Dependencies.Module} module
	     * @param {Boolean} [recursive=false]
	     * @returns {java.util.concurrent.RecursiveAction}
	     */
	    Public.identifyModuleTask = function(module, recursive) {
    		return function() {
    			try {
    				this.identifyModule(module, recursive)
    			}
    			catch (x) {
    				this.notify({type: 'error', message: 'Identification error for ' + module.specification.toString() + ': ' + x.message, exception: x})
    			}
    			return null
    		}.toTask('callable', this)
	    }

	    Public.findConflicts = function() {
	    	var potentialConflicts = []
	    	for (var m in this.identifiedModules) {
	    		potentialConflicts.push(this.identifiedModules[m])
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
	    	
	    	// Sort
	    	for (var c in this.conflicts) {
	    		var conflicts = this.conflicts[c]
	    		conflicts.sort(function(module1, module2) {
	    			// Reverse newness order
	    			return module2.identifier.compare(module1.identifier)
	    		})
	    	}
	    }

	    Public.resolveConflicts = function() {
	    	this.findConflicts()

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
					this.notify({type: 'error', message: 'Unsupported conflict policy: ' + this.conflictPolicy})
	    			continue
	    		}
	    		
	    		var chosenModule = conflicts[chosenModuleIndex]
	    		conflicts.splice(chosenModuleIndex, 1)

				this.notify('Resolved ' + (conflicts.length + 1) + '-way conflict to ' + chosenModule.identifier.toString() + ' in ' + chosenModule.identifier.repository.id + ' repository')

	    		// Merge all supplicants into chosen module, and remove non-chosen modules from identifiedModules
	    		for (var m in conflicts) {
	    			var module = conflicts[m]
	    			chosenModule.merge(module)
	    			this.removeIdentifiedModule(module)
	    			this.replaceModule(module, chosenModule)
	    		}
	    	}
	    }

	    /**
	     * Installs the identified modules.
	     */
	    Public.install = function(directory, overwrite, parallel) {
	    	parallel = Sincerity.Objects.ensure(parallel, true)
	    	
	    	var id = Sincerity.Objects.uniqueString()
			this.notify({type: 'begin', id: id, message: 'Installing'})

			var futures = []
	    	
	    	for (var m in this.identifiedModules) {
	    		var module = this.identifiedModules[m]
	    		if (parallel) {
	    			futures.push(module.identifier.repository.installModuleFuture(module.identifier, directory, overwrite, this))
	    		}
	    		else {
	    			module.identifier.repository.installModule(module.identifier, directory, overwrite, this)
	    		}
	    	}
	    	
			// Block until futures finish
			//try {
				for (var f in futures) {
					futures[f].get(1, java.util.concurrent.TimeUnit.HOURS)
				}
			/*}
    		catch (x) {
    			this.release()
    			if (Sincerity.JVM.isException(x, java.lang.InterruptedException)) {
    				java.lang.Thread.currentThread().interrupt()
    			}
    		}*/
			
			this.notify({type: 'end', id: id, message: 'Installed ' + futures.length + ' modules'})
	    }

	    Public.applyRules = function(module, context) {
			for (var r in this.rules) {
				var rule = this.rules[r]
				var command = null
				
				// Try repositories
				var repository
				for (var rr in this.repositories) {
					repository = this.repositories[rr]
					command = repository.applyModuleRule(module, rule, this)
    				if (command) {
    					break
    				}
    			}
				
				if (null === command) {
					this.notify({type: 'error', message: 'Unsupported rule: ' + rule.type})
					continue
				}

				if (true === command) {
					continue
				}

				if (Sincerity.Objects.isString(command)) {
					command = {type: command}
				}

				// Do command
				if (command.type == 'excludeModule') {
					this.notify('Excluded ' + module.specification.toString())
					context.exclude = true
				}
				else if (command.type == 'excludeDependencies') {
					this.notify('Excluded dependencies for ' + module.specification.toString())
					context.recursive = false
				}
				else if (command.type == 'setRepositories') {
					context.repositories = []
					for (var i in command.repositories) {
						var id = command.repositories[i]
						var found = false
						for (var rr in this.repositories) {
							var repository = this.repositories[rr]
							if (repository.id == id) {
								context.repositories.push(repository)
								found = true
								break
							}
						}
						if (!found) {
							this.notify({type: 'error', message: 'Unknown repository: ' + id})
						}
					}
					var ids = []
					for (var rr in context.repositories) {
						var repository = context.repositories[rr]
						ids.push(repository.id)
					}
					this.notify('Forced ' + module.specification.toString() + ' to identify in ' + ids.join(', ') + (ids.length > 1 ? ' repositories' : ' repository'))
				}
				else {
					this.notify({type: 'error', message: 'Unsupported command: ' + command.type})
				}
			}
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

    function printTree(out, item, getLineFn, getChildrenFn, indent, patterns, seal) {
    	indent = indent || 0
    	patterns = patterns || []

    	// Indent
    	for (var i = indent; i > 0; i--) {
    		out.print(' ')
    	}

    	// Patterns
    	var patternsLength = patterns.length
    	if (patternsLength) {
	    	for (var p in patterns) {
	    		var pattern = patterns[p]
	    		if (p == patternsLength - 1) {
					// Last pattern depends on whether we are sealing
					if (seal) {
						pattern = patternsLength < 2 ? tree.L : tree._L
					}
					else {
						pattern = patternsLength < 2 ? tree.T : tree._T
					}
	    		}
				out.print(pattern)
	    	}
	    	out.print(tree.VV)
	    	if (seal) {
				// Erase the pattern after it was sealed
				patterns[patternsLength - 1] = patternsLength < 2 ? '  ' : '    '
	    	}
    	}
    	
    	// Item
    	out.println(getLineFn(item))
    	
    	// Recurse
    	if (getChildrenFn) {
    		var children = getChildrenFn(item)
	    	var childrenLength = children ? children.length : 0
	    	if (childrenLength) {
	    		patterns.push(!patternsLength ? tree.I : tree._I)
	    		for (var c in children) {
		    		var child = children[c]
		    		printTree(out, child, getLineFn, getChildrenFn, indent, patterns, c == childrenLength - 1)
	    		}
	    		patterns.pop()
	    	}
    	}
    }
	
	function newForkJoinPool(parallelism) {
		var pool = new java.util.concurrent.ForkJoinPool(parallelism, new java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory({
			newThread: function(pool) {
				var thread = java.util.concurrent.ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool)
    			thread.daemon = true
    			return thread
			}
		}), null, false)
		pools.push(pool)
		return pool
	}
	
	function newExecutor(parallelism) {
    	var pool = java.util.concurrent.Executors.newFixedThreadPool(parallelism, new java.util.concurrent.ThreadFactory({
    		newThread: function(runnable) {
    			var thread = java.util.concurrent.Executors.defaultThreadFactory().newThread(runnable)
    			thread.daemon = true
    			return thread
    		}
    	}))
		pools.push(pool)
		return pool
	}
	
	var pools = []

    var tree = {}
	tree.L = ' \u2514'
	tree._L = '  ' + tree.L;
	tree.T = ' \u251C'
	tree._T = '  ' + tree.T;
	tree.I = ' \u2502'
	tree._I = '  ' + tree.I;
	tree.VV = '\u2500\u2500'
	tree.LVV = tree.L + tree.VV;
	tree.TVV = tree.T + tree.VV;
	
	return Public
}()


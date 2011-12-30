
document.executeOnce('/savory/classes/')
document.executeOnce('/savory/objects/')
document.executeOnce('/restlet/')

importClass(com.threecrickets.sincerity.exception.SincerityException)

var Prudence = Prudence || function() {
	/** @exports Public as Prudence */
    var Public = {}
    
    Public.cleanUri = function(uri) {
    	uri = uri.replace(/\/\//g, '/') // no doubles
    	if (uri == '' || uri[0] != '/') { // always at the beginning
    		uri = '/' + uri
    	}
    	if ((uri != '/') && (uri[uri.length - 1] != '/')) { // always at the end
    		uri += '/'
    	}
    	return uri
    }

	/**
	 * @class
	 * @name Prudence.Application
	 */
    Public.Application = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Application */
    	var Public = {}
    	
	    /** @ignore */
	    Public._construct = function() {
    		this.root = Savory.Sincerity.here
        	this.settings = {}
        	this.globals = {}
    		this.hosts = {}
        	this.routes = {}
    	}

    	Public.create = function(component) {
    		importClass(
    			com.threecrickets.prudence.PrudenceApplication,
    			com.threecrickets.prudence.PrudenceRouter,
    			org.restlet.routing.Router,
    			org.restlet.routing.Template,
				java.util.concurrent.CopyOnWriteArrayList,
    			java.io.File)

    		this.component = component
    		this.context = component.context.createChildContext()
        	this.instance = new PrudenceApplication(this.context)
    		
    		// Description
    		if (Savory.Objects.exists(this.settings.description)) {
    			Savory.Objects.merge(this.instance, this.settings.description, ['name', 'description', 'author', 'owner'])
    		}
    		
    		if (!Savory.Objects.exists(this.hosts.internal)) {
    			this.hosts.internal = this.root.name
    		}
    		
        	// Attach to hosts
        	for (var name in this.hosts) {
        		var host = Restlet.getHost(component, name)
        		if (!Savory.Objects.exists(host)) {
        			throw new SavoryException('Unknown host: ' + name)
        		}
        		var uri = this.hosts[name]
        		if ((uri != '') && (uri[uri.length - 1] == '/')) {
        			// No trailing slash
        			uri = uri.slice(0, -1)
        		}
        		print('Attaching application to "' + uri + '/" on host "' + name + '"\n')
        		host.attach(uri, this.instance)
        	}

        	// Inbound root
        	this.instance.inboundRoot = new PrudenceRouter(this.context)
        	this.instance.inboundRoot.routingMode = Router.MODE_BEST_MATCH
        	
        	// Libraries
			this.libraryDocumentSources = new CopyOnWriteArrayList()

        	if (Savory.Objects.exists(this.settings.code.libraries)) {
    			for (var i in this.settings.code.libraries) {
    				var library = this.settings.code.libraries[i]
    				
    	    		if (!(library instanceof File)) {
    	    			library = new File(this.root, library).absoluteFile
    	    		}
    				
    				print('Adding library: "' + library + '"\n')
    				this.libraryDocumentSources.add(this.createDocumentSource(library))
    			}
        	}
        	
        	// Common library
        	var commonLibraryDocumentSource = component.context.attributes.get('prudence.commonLibraryDocumentSource')
        	if (!Savory.Objects.exists(commonLibraryDocumentSource)) {
	    		var library = sincerity.container.getLibrariesFile('scripturian')
				commonLibraryDocumentSource = this.createDocumentSource(library)
	    		component.context.attributes.put('prudence.commonLibraryDocumentSource', commonLibraryDocumentSource)
        	}
			print('Adding library: "' + commonLibraryDocumentSource.basePath + '"\n')
			this.libraryDocumentSources.add(commonLibraryDocumentSource)

        	// Sincerity library
        	var sincerityLibraryDocumentSource = component.context.attributes.get('prudence.sincerityLibraryDocumentSource')
        	if (!Savory.Objects.exists(sincerityLibraryDocumentSource)) {
	    		var library = sincerity.getHomeFile('libraries', 'scripturian')
				sincerityLibraryDocumentSource = this.createDocumentSource(library)
	    		component.context.attributes.put('prudence.sincerityLibraryDocumentSource', sincerityLibraryDocumentSource)
        	}
			print('Adding library: "' + sincerityLibraryDocumentSource.basePath + '"\n')
			this.libraryDocumentSources.add(sincerityLibraryDocumentSource)

        	// Create and attach restlets
        	for (var uri in this.routes) {
        		var restlet = this.routes[uri]

        		var attachBase = false
        		var length = uri.length
        		if (length > 1) {
        			var last = uri[length - 1]
	        		if (last == '*') {
	        			uri = uri.substring(0, length - 1)
	        			attachBase = true
	        		}
        		}
        		
        		uri = Module.cleanUri(uri)

        		restlet = this.createRestlet(restlet, uri)
        		if (Savory.Objects.exists(restlet)) {
	        		if (attachBase) {
	            		print('Attaching "' + uri + '*" to ' + restlet + '\n')
	        			this.instance.inboundRoot.attachBase(uri, restlet)
	        		}
	        		else {
	            		print('Attaching "' + uri + '" to ' + restlet + '\n')
	        			this.instance.inboundRoot.attach(uri, restlet, Template.MODE_EQUALS)
	        		}
        		}
        	}

        	// Apply globals
        	var globals = Savory.Objects.flatten(this.globals)
        	for (var g in globals) {
        		this.context.attributes.put(g, globals[g])
        	}
    	}
    	
    	Public.createRestlet = function(restlet, uri) {
    		if (Savory.Objects.isArray(restlet)) {
    			return new Module.Chain({restlets: restlet}).create(this, uri)
    		}
    		else if (Savory.Objects.isString(restlet)) {
    			if (restlet == 'hidden') {
            		print('Hiding "' + uri + '"\n')
    				this.instance.inboundRoot.hide(uri)
    				return null
    			}
    			else {
        			var type = Module[Savory.Objects.capitalize(restlet)]
    				if (Savory.Objects.exists(type)) {
    					return new type().create(this, uri)
    				}
    				else {
    					return new Module.Implicit({id: restlet}).create(this, uri)
    				}
    			}
    		}
    		else if (Savory.Objects.isString(restlet.type)) {
    			var type = Module[Savory.Objects.capitalize(restlet.type)]
    			delete restlet.type
    			return new type(restlet).create(this, uri)        			
    		}
    		else {
    			return restlet.create(this, uri)
    		}
    	}
    	
    	Public.createDocumentSource = function(root, preExtension, defaultDocumentName, defaultExtension) {
    		importClass(
    			com.threecrickets.scripturian.document.DocumentFileSource)

        	return new DocumentFileSource(
				String(root),
				root,
				defaultDocumentName || this.settings.code.defaultDocumentName,
				defaultExtension || this.settings.code.defaultExtension,
				Savory.Objects.ensure(preExtension, null),
				this.settings.code.minimumTimeBetweenValidityChecks
			)
    	}
    	
    	return Public    
    }(Public))

	/**
	 * @class
	 * @name Prudence.Resource
	 */
    Public.Resource = Savory.Classes.define(function() {
		/** @exports Public as Prudence.Resource */
    	var Public = {}
    	
    	Public.create = function(app, uri) {
    	}
    	
    	return Public
    }())

	/**
	 * @class
	 * @name Prudence.StaticWeb
	 * @augments Prudence.Resource
	 */
    Public.StaticWeb = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.StaticWeb */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = ['root', 'listingAllowed', 'negotiatingContent']

    	Public.create = function(app, uri) {
    		importClass(
    			org.restlet.resource.Directory,
    			java.io.File)
    		
    		this.root = Savory.Objects.ensure(this.root, 'mapped')
    		if (!(this.root instanceof File)) {
    			this.root = new File(app.root, this.root).absoluteFile
    		}
    		
    		var directory = new Directory(app.context, this.root.toURI())
    		directory.listingAllowed = this.listingAllowed || false
    		directory.negotiatingContent = this.negotiatingContent || true
    		return directory
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.DynamicWeb
	 * @augments Prudence.Resource
	 */
    Public.DynamicWeb = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.DynamicWeb */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = ['root', 'fragmentsRoot', 'passThroughs', 'preExtension', 'defaultDocumentName', 'defaultExtension', 'clientCachingMode']

    	Public.create = function(app, uri) {
    		if (Savory.Objects.exists(app.globals['com.threecrickets.prudence.GeneratedTextResource'])) {
    			throw new SincerityException('There can be only one DynamicWeb per application')
    		}

    		importClass(
    			org.restlet.resource.Finder,
    			com.threecrickets.prudence.util.PhpExecutionController,
    			java.util.concurrent.CopyOnWriteArrayList,
    			java.util.concurrent.CopyOnWriteArraySet,
    			java.util.concurrent.ConcurrentHashMap,
    			java.io.File)
    			
    		this.root = Savory.Objects.ensure(this.root, 'mapped')
    		if (!(this.root instanceof File)) {
    			this.root = new File(app.root, this.root).absoluteFile
    		}
    		
    		print('DynamicWeb at "' + this.root + '"\n')
    		
    		this.fragmentsRoot = Savory.Objects.ensure(this.fragmentsRoot, 'fragments')
    		if (!(this.fragmentsRoot instanceof File)) {
    			this.fragmentsRoot = new File(app.root, this.fragmentsRoot).absoluteFile
    		}

    		if (Savory.Objects.isString(this.clientCachingMode)) {
	    		if (this.clientCachingMode == 'disabled') {
	    			this.clientCachingMode = 0
	    		}
	    		else if (this.clientCachingMode == 'conditional') {
	    			this.clientCachingMode = 1
	    		}
	    		else if (this.clientCachingMode == 'offline') {
	    			this.clientCachingMode = 2
	    		}
	    		else {
        			throw new SavoryException('Unsupported clientCachingMode: ' + this.clientCachingMode)
	    		}
    		}
    		else if (!Savory.Objects.exists(this.clientCachingMode)) {
    			this.clientCachingMode = 1
    		}

    		this.defaultDocumentName = this.defaultDocumentName || 'index'
    		this.defaultExtension = this.defaultExtension || 'html'

    		if (undefined === this.preExtension) {
    			this.preExtension = 'd'
    		}

    		var generatedTextResource = app.globals['com.threecrickets.prudence.GeneratedTextResource'] = {
    			documentSource: app.createDocumentSource(this.root, this.preExtension, this.defaultDocumentName, this.defaultExtenion),
	    		extraDocumentSources: new CopyOnWriteArrayList(),
	    		libraryDocumentSources: app.libraryDocumentSources,
	    		passThroughDocuments: new CopyOnWriteArraySet(),
	    		cacheKeyPatternHandlers: new ConcurrentHashMap(),
	    		scriptletPlugins: new ConcurrentHashMap(),
	    		clientCachingMode: this.clientCachingMode,
	    		defaultIncludedName: this.defaultDocumentName,
	    		executionController: new PhpExecutionController(), // Adds PHP predefined variables
    			languageManager: executable.manager
    		}

    		// Fragments
    		if (Savory.Objects.exists(this.fragmentsRoot)) {
    			generatedTextResource.extraDocumentSources.add(app.createDocumentSource(this.fragmentsRoot, null, this.defaultDocumentName, this.defaultExtenion))
    		}

        	// Common fragments
        	var commonFragmentsDocumentSource = app.component.context.attributes.get('prudence.fragmentsDocumentSource')
        	if (!Savory.Objects.exists(commonFragmentsDocumentSource)) {
	    		var library = sincerity.container.getFile('component', 'fragments')
				commonFragmentsDocumentSource = app.createDocumentSource(library, null, this.defaultDocumentName, this.defaultExtenion)
	    		app.component.context.attributes.put('prudence.fragmentsDocumentSource', commonFragmentsDocumentSource)
        	}

        	generatedTextResource.extraDocumentSources.add(commonFragmentsDocumentSource)
    		
    		if (Savory.Objects.exists(this.passThroughs)) {
	    		for (var i in this.passThroughs) {
	    			print('Pass through: "' + this.passThroughs[i] + '"\n')
	    			generatedTextResource.passThroughDocuments.add(this.passThroughs[i])
	    		}
    		}
    		
    		return new Finder(app.context, Savory.Sincerity.getClass('com.threecrickets.prudence.GeneratedTextResource'))
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.Explicit
	 * @augments Prudence.Resource
	 */
    Public.Explicit = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Explicit */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = ['root', 'passThroughs', 'implicit', 'preExtension']

    	Public.create = function(app, uri) {
    		if (Savory.Objects.exists(app.globals['com.threecrickets.prudence.DelegatedResource'])) {
    			throw new SincerityException('There can be only one Explicit per application')
    		}

    		importClass(
    			org.restlet.resource.Finder,
    			java.util.concurrent.CopyOnWriteArraySet,
    			java.io.File)

    		this.root = Savory.Objects.ensure(this.root, 'mapped')
    		if (!(this.root instanceof File)) {
    			this.root = new File(app.root, this.root).absoluteFile
    		}

    		if (undefined === this.preExtension) {
    			this.preExtension = 'e'
    		}
    		
    		app.implicit = this.implicit = this.implicit || {}
    		this.implicit.routerDocumentName = this.implicit.routerDocumentName || '/prudence/implicit/'

    		var delegatedResource = app.globals['com.threecrickets.prudence.DelegatedResource'] = {
    			documentSource: app.createDocumentSource(this.root, this.preExtension),
	    		libraryDocumentSources: app.libraryDocumentSources,
	    		passThroughDocuments: new CopyOnWriteArraySet(),
	    		defaultName: app.settings.code.defaultDocumentName,
	    		defaultLanguageTag: app.settings.code.defaultLanguageTag,
	    		languageManager: executable.manager
    		}

    		if (Savory.Objects.exists(this.passThroughs)) {
	    		for (var i in this.passThroughs) {
	    			print('Pass through: "' + this.passThroughs[i] + '"\n')
	    			delegatedResource.passThroughDocuments.add(this.passThroughs[i])
	    		}
    		}

    		// Implicit router
    		delegatedResource.passThroughDocuments.add(this.implicit.routerDocumentName)
       		app.implicit.routerUri = Module.cleanUri(uri + this.implicit.routerDocumentName)
    		app.instance.inboundRoot.hide(app.implicit.routerUri)

    		return new Finder(app.context, Savory.Sincerity.getClass('com.threecrickets.prudence.DelegatedResource'))
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.Implicit
	 * @augments Prudence.Resource
	 */
    Public.Implicit = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Implicit */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = ['id', 'locals']

    	Public.create = function(app, uri) {
    		importClass(
    			com.threecrickets.prudence.util.Injector,
    			com.threecrickets.prudence.util.CapturingRedirector)
    			
       		if (!Savory.Objects.exists(app.implicit)) {
    			throw new SincerityException('An Explicit must be attached before an Implicit can be created')
       		}
    		
    		app.implicit.resourcesDocumentName = app.implicit.resourcesDocumentName || '/resources/'

       		var capture = new CapturingRedirector(app.context, 'riap://application' + app.implicit.routerUri + '?{rq}', false)
    		var injector = new Injector(app.context, capture)
    		injector.values.put('prudence.id', this.id)

    		if (Savory.Objects.exists(this.locals)) {
    			for (var i in this.locals) {
    				injector.values.put(i, this.locals[i])
    			}
    		}
   
        	app.globals['prudence.implicit.resourcesDocumentName'] = app.implicit.resourcesDocumentName
   
    		return injector
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.Chain
	 * @augments Prudence.Resource 
	 */
    Public.Chain = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Chain */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = ['restlets']

    	Public.create = function(app, uri) {
    		importClass(com.threecrickets.prudence.util.Fallback)
    		
    		var fallback = new Fallback(app.context, app.settings.code.minimumTimeBetweenValidityChecks)
    		
    		for (var i in this.restlets) {
    			var restlet = app.createRestlet(this.restlets[i], uri)
    			if (Savory.Objects.exists(restlet)) {
    				fallback.addTarget(restlet)    				
    			}
    		}
    		
    		return fallback
    	}
    	
    	return Public
    }(Public))

    return Public
}()
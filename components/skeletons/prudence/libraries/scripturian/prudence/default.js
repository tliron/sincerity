
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
    		this.hosts = {}
        	this.routes = {}
        	this.resources = {}
        	this.globals = {}
    	}

    	Public.create = function(component) {
    		importClass(
    			com.threecrickets.prudence.PrudenceApplication,
    			com.threecrickets.prudence.PrudenceRouter,
				com.threecrickets.scripturian.document.DocumentFileSource,
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
        	
        	// Libraries
			this.libraryDocumentSources = new CopyOnWriteArrayList()

        	if (Savory.Objects.exists(this.settings.code.libraries)) {
    			for (var i in this.settings.code.libraries) {
    				var library = this.settings.code.libraries[i]
    				
    	    		if (!(library instanceof File)) {
    	    			library = new File(this.root, library).absoluteFile
    	    		}
    				
    				print('Adding library: "' + library + '"\n')
    				
    				this.libraryDocumentSources.add(new DocumentFileSource(
    					library,
    					library,
    					this.settings.code.defaultDocumentName,
    					this.settings.code.defaultExtension,
    					this.settings.code.minimumTimeBetweenValidityChecks
    				))
    			}
        	}
        	
        	// Common library
        	var commonLibraryDocumentSource = component.context.attributes.get('prudence.commonLibraryDocumentSource')
        	if (!Savory.Objects.exists(commonLibraryDocumentSource)) {
	    		var library = sincerity.container.getLibrariesFile('scripturian')
				commonLibraryDocumentSource = new DocumentFileSource(
					library,
					library,
					this.settings.code.defaultDocumentName,
					this.settings.code.defaultExtension,
					this.settings.code.minimumTimeBetweenValidityChecks
				)
	    		component.context.attributes.put('prudence.commonLibraryDocumentSource', commonLibraryDocumentSource)
        	}
			print('Adding library: "' + commonLibraryDocumentSource.basePath + '"\n')
			this.libraryDocumentSources.add(commonLibraryDocumentSource)

        	// Sincerity library
        	var sincerityLibraryDocumentSource = component.context.attributes.get('prudence.sincerityLibraryDocumentSource')
        	if (!Savory.Objects.exists(sincerityLibraryDocumentSource)) {
	    		var library = sincerity.getHomeFile('libraries', 'scripturian')
				sincerityLibraryDocumentSource = new DocumentFileSource(
					library,
					library,
					this.settings.code.defaultDocumentName,
					this.settings.code.defaultExtension,
					this.settings.code.minimumTimeBetweenValidityChecks
				)
	    		component.context.attributes.put('prudence.sincerityLibraryDocumentSource', sincerityLibraryDocumentSource)
        	}
			print('Adding library: "' + sincerityLibraryDocumentSource.basePath + '"\n')
			this.libraryDocumentSources.add(sincerityLibraryDocumentSource)

        	// Create and attach restlets
        	for (var uri in this.uris) {
        		var restlet = this.uris[uri]
        		
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

        		if (Savory.Objects.isString(restlet)) {
        			if (restlet == 'hidden') {
                		print('Hiding "' + uri + '"\n')
        				this.instance.inboundRoot.hide(uri)
        				continue
        			}
        			else {
            			var type = Module[Savory.Objects.capitalize(restlet)]
        				if (Savory.Objects.exists(type)) {
        					restlet = new type().create(this, uri)
        				}
        				else {
        					restlet = new Module.Implicit({id: restlet}).create(this, uri)
        				}
        			}
        		}
        		else if (Savory.Objects.isString(restlet.type)) {
        			var type = Module[Savory.Objects.capitalize(restlet.type)]
        			delete restlet.type
        			restlet = new type(restlet).create(this, uri)        			
        		}
        		else {
        			restlet = restlet.create(this, uri)
        		}
        		
        		if (attachBase) {
            		print('Attaching "' + uri + '*" to ' + restlet + '\n')
        			this.instance.inboundRoot.attachBase(uri, restlet)
        		}
        		else {
            		print('Attaching "' + uri + '" to ' + restlet + '\n')
        			this.instance.inboundRoot.attach(uri, restlet, Template.MODE_EQUALS)
        		}
        	}

        	// Apply globals
        	var globals = Savory.Objects.flatten(this.globals)
        	for (var g in globals) {
        		this.context.attributes.put(g, globals[g])
        	}
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
	 * @param {Array} array The array
	 */
    Public.StaticWeb = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.StaticWeb */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = ['root', 'listingAllowed']

    	Public.create = function(app, uri) {
    		importClass(
    			org.restlet.resource.Directory,
    			java.io.File)
    		
    		if (!(this.root instanceof File)) {
    			this.root = new File(app.root, this.root).absoluteFile
    		}
    		
    		var directory = new Directory(app.context, this.root.toURI())
    		directory.listingAllowed = this.listingAllowed || false
    		directory.negotiatingContent = true
    		return directory
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.DynamicWeb
	 * @augments Prudence.Resource 
	 * @param {Array} array The array
	 */
    Public.DynamicWeb = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.DynamicWeb */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

    	Public.create = function(app, uri) {
    		if (Savory.Objects.exists(app.globals['com.threecrickets.prudence.GeneratedTextResource'])) {
    			throw new SincerityException('There can be only one DynamicWeb per application')
    		}

    		importClass(
    			org.restlet.resource.Finder,
    			com.threecrickets.scripturian.document.DocumentFileSource,
    			com.threecrickets.prudence.util.PhpExecutionController,
    			java.util.concurrent.CopyOnWriteArrayList,
    			java.util.concurrent.CopyOnWriteArraySet,
    			java.util.concurrent.ConcurrentHashMap,
    			java.io.File)
    			
    		var settings = app.resources.dynamicWeb
    		
    		if (!(settings.root instanceof File)) {
    			settings.root = new File(app.root, settings.root).absoluteFile
    		}
    		
    		print('DynamicWeb at "' + settings.root + '"\n')
    		
    		if (!(settings.fragmentsRoot instanceof File)) {
    			settings.fragmentsRoot = new File(app.root, settings.fragmentsRoot).absoluteFile
    		}

    		if (Savory.Objects.isString(settings.clientCachingMode)) {
	    		if (settings.clientCachingMode == 'disabled') {
	    			settings.clientCachingMode = 0
	    		}
	    		else if (settings.clientCachingMode == 'conditional') {
	    			settings.clientCachingMode = 1
	    		}
	    		else if (settings.clientCachingMode == 'offline') {
	    			settings.clientCachingMode = 2
	    		}
	    		else {
        			throw new SavoryException('Unsupported clientCachingMode: ' + settings.clientCachingMode)
	    		}
    		}
    		else if (!Savory.Objects.exists(settings.clientCachingMode)) {
    			settings.clientCachingMode = 1
    		}

    		settings.defaultDocumentName = settings.defaultDocumentName || 'index'
    		settings.defaultExtension = settings.defaultExtension || 'html'

    		var generatedTextResource = app.globals['com.threecrickets.prudence.GeneratedTextResource'] = {
    			documentSource: new DocumentFileSource(
    				settings.root,
    				settings.root,
    				settings.defaultDocumentName,
    				settings.defaultExtenion,
    				app.settings.code.minimumTimeBetweenValidityChecks
    			),
	    		extraDocumentSources: new CopyOnWriteArrayList(),
	    		libraryDocumentSources: app.libraryDocumentSources,
	    		passThroughDocuments: new CopyOnWriteArraySet(),
	    		cacheKeyPatternHandlers: new ConcurrentHashMap(),
	    		scriptletPlugins: new ConcurrentHashMap(),
	    		clientCachingMode: settings.clientCachingMode,
	    		defaultIncludedName: settings.defaultDocumentName,
	    		executionController: new PhpExecutionController(), // Adds PHP predefined variables
    			languageManager: executable.manager
    		}

    		// Fragments
    		if (Savory.Objects.exists(settings.fragmentsRoot)) {
    			generatedTextResource.extraDocumentSources.add(new DocumentFileSource(
    				settings.fragmentsRoot,
    				settings.fragmentsRoot,
    				settings.defaultDocumentName,
    				settings.defaultExtenion,
    				app.settings.code.minimumTimeBetweenValidityChecks
    			))
    		}

        	// Common fragments
        	var commonFragmentsDocumentSource = app.component.context.attributes.get('prudence.fragmentsDocumentSource')
        	if (!Savory.Objects.exists(commonFragmentsDocumentSource)) {
	    		var library = sincerity.container.getFile('component', 'fragments')
				commonFragmentsDocumentSource = new DocumentFileSource(
					library,
					library,
    				settings.defaultDocumentName,
    				settings.defaultExtenion,
    				app.settings.code.minimumTimeBetweenValidityChecks
				)
	    		app.component.context.attributes.put('prudence.fragmentsDocumentSource', commonFragmentsDocumentSource)
        	}

        	generatedTextResource.extraDocumentSources.add(commonFragmentsDocumentSource)
    		
    		if (settings.passThroughs) {
	    		for (var i in settings.passThroughs) {
	    			print('Pass through: "' + settings.passThroughs[i] + '"\n')
	    			generatedTextResource.passThroughDocuments.add(settings.passThroughs[i])
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
	 * @param {Array} array The array
	 */
    Public.Explicit = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Explicit */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

    	Public.create = function(app, uri) {
    		if (Savory.Objects.exists(app.globals['com.threecrickets.prudence.DelegatedResource'])) {
    			throw new SincerityException('There can be only one Explicit per application')
    		}

    		importClass(
    			org.restlet.resource.Finder,
    			com.threecrickets.scripturian.document.DocumentFileSource,
    			java.util.concurrent.CopyOnWriteArraySet,
    			java.io.File)

    		var settings = app.resources.explicit

    		if (!(settings.root instanceof File)) {
    			settings.root = new File(app.root, settings.root).absoluteFile
    		}
    		
    		settings.implicitRouterDocumentName = settings.implicitRouterDocumentName || '/prudence/implicit/'

    		var delegatedResource = app.globals['com.threecrickets.prudence.DelegatedResource'] = {
    			documentSource: new DocumentFileSource(
    				settings.root,
    				settings.root,
    				app.settings.code.defaultDocumentName,
    				app.settings.code.defaultExtenion,
    				app.settings.code.minimumTimeBetweenValidityChecks
    			),
	    		libraryDocumentSources: app.libraryDocumentSources,
	    		passThroughDocuments: new CopyOnWriteArraySet(),
	    		defaultName: app.settings.code.defaultDocumentName,
	    		defaultLanguageTag: app.settings.code.defaultLanguageTag,
	    		languageManager: executable.manager
    		}

    		if (settings.passThroughs) {
	    		for (var i in settings.passThroughs) {
	    			print('Pass through: "' + settings.passThroughs[i] + '"\n')
	    			delegatedResource.passThroughDocuments.add(settings.passThroughs[i])
	    		}
    		}

    		// Implicit router
    		delegatedResource.passThroughDocuments.add(settings.implicitRouterDocumentName)
       		settings.implicitRouterUri = Module.cleanUri(uri + settings.implicitRouterDocumentName)
    		app.instance.inboundRoot.hide(settings.implicitRouterUri)

    		return new Finder(app.context, Savory.Sincerity.getClass('com.threecrickets.prudence.DelegatedResource'))
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.Implicit
	 * @augments Prudence.Resource 
	 * @param {Array} array The array
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
    			
       		var settings = app.resources.implicit
       		var explicitSettings = app.resources.explicit
       		
       		if (!Savory.Objects.exists(explicitSettings.implicitRouterUri)) {
    			throw new SincerityException('An Explicit must be attached before an Implicit can be created')
       		}
    		
    		settings.resourcesDocumentName = settings.resourcesDocumentName || '/resources/'

       		var capture = new CapturingRedirector(app.context, 'riap://application' + explicitSettings.implicitRouterUri + '?{rq}', false)
    		var injector = new Injector(app.context, capture)
    		injector.values.put('prudence.id', this.id)
    		if (Savory.Objects.exists(this.locals)) {
    			for (var i in this.locals) {
    				injector.values.put(i, this.locals[i])
    			}
    		}
   
        	app.globals['prudence.implicit.resourcesDocumentName'] = settings.resourcesDocumentName
   
    		return injector
    	}
    	
    	return Public
    }(Public))
	
    return Public
}()
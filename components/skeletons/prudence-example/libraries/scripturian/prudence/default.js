

document.executeOnce('/savory/classes/')
document.executeOnce('/savory/objects/')
document.executeOnce('/restlet/')

var Prudence = Prudence || function() {
	/** @exports Public as Prudence */
    var Public = {}
    
    Public.cleanUri = function(uri) {
    	uri = uri.replace(/\/\//g, '/') // no doubles
    	if(uri == '' || uri[0] != '/') { // always at the beginning
    		uri = '/' + uri
    	}
    	if((uri != '/') && (uri[uri.length - 1] != '/')) { // always at the end
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
    	
    	Public.settings = {}
    	
    	Public.routes = {}

    	Public.resources = {}

    	Public.globals = {}

	    /** @ignore */
	    Public._construct = function() {
    		this.root = Savory.Sincerity.here
    	}

    	Public.create = function(component) {
    		importClass(
    			com.threecrickets.prudence.PrudenceApplication,
    			com.threecrickets.prudence.PrudenceRouter,
				com.threecrickets.scripturian.document.DocumentFileSource,
    			org.restlet.routing.Template,
				java.util.concurrent.CopyOnWriteArrayList,
    			java.io.File)

    		// The context
    		this.context = component.context.createChildContext()
    		
        	// The application
        	this.instance = new PrudenceApplication(this.context)
    		
    		// Descriptions
    		if (this.settings.description) {
    			Savory.Objects.merge(this.instance, this.settings.description)
    		}
    		
    		// Attach to internal router
    		component.internalRouter.attach('/example', this.instance).matchingMode = Template.MODE_STARTS_WITH
    		
        	// Attach to hosts
        	for (var name in this.settings.hosts) {
        		var host = Restlet.getHost(component, name)
        		if (!Savory.Objects.exists(host)) {
        			print('Unknown host: ' + name + '\n')
        		}
        		var uri = this.settings.hosts[name]
        		if((uri != '') && (uri[uri.length - 1] == '/')) {
        			// No trailing slash
        			uri = uri.slice(0, -1)
        		}
        		print('Attaching to ' + uri + ' on ' + name + '\n')
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
    				
    				print('Adding library: ' + library + '\n')
    				
    				this.libraryDocumentSources.add(new DocumentFileSource(library, library, this.settings.code.defaultDocumentName, this.settings.code.defaultExtension, this.settings.code.minimumTimeBetweenValidityChecks))
    			}
        	}

    		var library = sincerity.container.getLibrariesFile('scripturian')
			print('Adding library: ' + library + '\n')
			this.libraryDocumentSources.add(new DocumentFileSource(library, library, this.settings.code.defaultDocumentName, this.settings.code.defaultExtension, this.settings.code.minimumTimeBetweenValidityChecks))

        	// Attach restlets
        	for (var uri in this.routes) {
        		var restlet = this.routes[uri]
        		
        		var mode = Template.MODE_EQUALS
        		var length = uri.length
        		if (length > 1) {
        			var last = uri[length - 1]
	        		if (last == '*') {
	        			uri = uri.substring(0, length - 1)
	        			mode = Template.MODE_STARTS_WITH
	        		}
        		}
        		
        		uri = Module.cleanUri(uri)

        		if (Savory.Objects.isString(restlet)) {
        			if (restlet == 'hidden') {
                		print('Hiding ' + uri + '\n')
        				this.instance.inboundRoot.hide(uri)
        				continue
        			}
        			else {
            			var type = Module[Savory.Objects.capitalize(restlet)]
        				if (Savory.Objects.exists(type)) {
        					restlet = new type().create(this)
        				}
        				else {
        					restlet = new Module.Internal({id: restlet}).create(this)
        				}
        			}
        		}
        		else if (Savory.Objects.isString(restlet.type)) {
        			var type = Module[Savory.Objects.capitalize(restlet.type)]
        			delete restlet.type
        			restlet = new type(restlet).create(this)        			
        		}
        		else {
        			restlet = restlet.create(this)
        		}
        		
        		print('Attaching ' + uri + ' to ' + restlet + '\n')
        		if (null !== mode) {
        			this.instance.inboundRoot.attach(uri, restlet, mode)
        		}
        		else {
        			this.instance.inboundRoot.attach(uri, restlet)
        		}
        	}

        	// Globals
        	var globals = Savory.Objects.flatten(this.globals)
        	
        	// Router for internal resources
        	if (Savory.Objects.exists(globals['com.threecrickets.prudence.DelegatedResource.passThroughDocuments'])) {
        		print('Adding internal router\n')
        		globals['com.threecrickets.prudence.DelegatedResource.passThroughDocuments'].add('/prudence/internal/')
            	globals['prudence.internal'] = this.resources.internal.resources
        		this.instance.inboundRoot.hide(app.resources.internal.explicit + 'prudence/internal/')
        	}

        	// Apply globals
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
    	
    	Public.create = function(settings, context) {
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

    	Public.create = function(app) {
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

    	Public.create = function(app) {
    		if (Savory.Objects.exists(app.globals['com.threecrickets.prudence.GeneratedTextResource'])) {
    			throw 'There can be only one DynamicWeb per application'
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
    		
    		print('DynamicWeb at ' + settings.root + '\n')
    		
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
    		}
    		else if (!Savory.Objects.exists(settings.clientCachingMode)) {
    			settings.clientCachingMode = 1
    		}

    		settings.defaultDocumentName = settings.defaultDocumentName || 'index'
    		settings.defaultExtension = settings.defaultExtension || 'html'

    		var generatedTextResource = app.globals['com.threecrickets.prudence.GeneratedTextResource'] = {
    			documentSource: new DocumentFileSource(settings.root, settings.root, settings.defaultDocumentName, settings.defaultExtenion, app.settings.code.minimumTimeBetweenValidityChecks),
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

    		if (Savory.Objects.exists(settings.fragmentsRoot)) {
    			generatedTextResource.extraDocumentSources.add(new DocumentFileSource(settings.fragmentsRoot, settings.fragmentsRoot, settings.defaultDocumentName, settings.defaultExtenion, app.settings.code.minimumTimeBetweenValidityChecks))
    		}

    		//ourSettings.extraDocumentSources.add(commonFragmentsDocumentSource)
    		
    		if (settings.passThroughs) {
	    		for (var i in settings.passThroughs) {
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

    	Public.create = function(app) {
    		if (Savory.Objects.exists(app.globals['com.threecrickets.prudence.DelegatedResource'])) {
    			throw 'There can be only one Resources per application'
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

    		var delegatedResource = app.globals['com.threecrickets.prudence.DelegatedResource'] = {
    			documentSource: new DocumentFileSource(settings.root, settings.root, app.settings.code.defaultDocumentName, app.settings.code.defaultExtenion, app.settings.code.minimumTimeBetweenValidityChecks),
	    		libraryDocumentSources: app.libraryDocumentSources,
	    		passThroughDocuments: new CopyOnWriteArraySet(),
	    		defaultName: app.settings.code.defaultDocumentName,
	    		defaultLanguageTag: app.settings.code.defaultLanguageTag,
	    		languageManager: executable.manager
    		}

    		if (settings.passThroughs) {
	    		for (var i in settings.passThroughs) {
	    			print('Pass through: ' + settings.passThroughs[i] + '\n')
	    			delegatedResource.passThroughDocuments.add(settings.passThroughs[i])
	    		}
    		}
    		
    		return new Finder(app.context, Savory.Sincerity.getClass('com.threecrickets.prudence.DelegatedResource'))
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.Internal
	 * @augments Prudence.Resource 
	 * @param {Array} array The array
	 */
    Public.Internal = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Internal */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = ['id']

    	Public.create = function(app) {
    		importClass(
    			com.threecrickets.prudence.util.Injector,
    			com.threecrickets.prudence.util.CapturingRedirector)
    			
       		var settings = app.resources.internal
       		
       		var capture = new CapturingRedirector(app.context, 'riap://application' + settings.explicit + 'prudence/internal/?{rq}', false)
    		var injector = new Injector(app.context, capture)
    		injector.values.put('id', this.id)
    		
    		return injector
    	}
    	
    	return Public
    }(Public))
	
    return Public
}()
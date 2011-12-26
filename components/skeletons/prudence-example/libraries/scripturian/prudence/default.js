

document.executeOnce('/savory/classes/')
document.executeOnce('/savory/objects/')
document.executeOnce('/restlet/')

var Prudence = Prudence || function() {
	/** @exports Public as Prudence */
    var Public = {}
    
    Public.cleanUri = function(uri) {
		if ((uri.length > 0) && (uri[0] == '/')) {
			uri = uri.substring(1)
		}
		return uri
    }
    
    Public.createApplication = function(settings, component) {
    }

    Public.Application = Savory.Classes.define(function(Module) {
    	var Public = {}
    	
    	Public.settings = {}
    	
    	Public.routes = {}
    	
    	Public.globals = {}

    	Public.create = function(component) {
    		// The context
    		this.context = component.context.createChildContext()
    		
        	// The application
        	this.instance = new com.threecrickets.prudence.PrudenceApplication(this.context)
        	Savory.Objects.merge(this.instance, this.settings.application)

        	// Attach to hosts
        	for (var name in this.settings.hosts) {
        		Restlet.getHost(component, name).attach(this.settings.hosts[name], this.instance)
        	}

        	// Inbound root
        	this.instance.inboundRoot = new com.threecrickets.prudence.PrudenceRouter(app.context)

        	// Attach routes
        	for (var uri in this.routes) {
        		var restlet = this.routes[uri].create(this)
        		
        		print('Attaching ' + uri + ' to ' + restlet + '\n')
        		if (uri[0] == '=') {
        			uri = uri.substring(1)
        			this.instance.inboundRoot.attach(Module.cleanUri(uri), restlet, org.restlet.routing.Template.MODE_EQUALS)
        		}
        		else {
        			this.instance.inboundRoot.attach(Module.cleanUri(uri), restlet)
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

	    Public._construct = function(config) {
    		print ('root: ' + this.root + '\n')
    		if (!(this.root instanceof java.io.File)) {
    			this.root = new java.io.File(Savory.Sincerity.here.parentFile, this.root).absoluteFile
    		}
    		print ('root: ' + this.root + '\n')
    	}

    	Public.create = function(app) {
    		var directory = new org.restlet.resource.Directory(app.context, this.root.toURI())
    		directory.listingAllowed = this.listingAllowed || false
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

		/** @ignore */
    	Public._configure = ['root', 'fragmentsRoot', 'defaultDocument', 'defaultExtension', 'minimumTimeBetweenValidityChecks']

	    /** @ignore */
	    Public._construct = function(config) {
    		if (!(this.root instanceof java.io.File)) {
    			this.root = new java.io.File(Savory.Sincerity.here.parentFile, this.root).absoluteFile
    		}
    		if (!(this.fragmentsRoot instanceof java.io.File)) {
    			this.fragmentsRoot = new java.io.File(Savory.Sincerity.here.parentFile, this.fragmentsRoot).absoluteFile
    		}
    		this.defaultDocument = this.defaultDocument || 'index'
    		this.defaultExtension = this.defaultExtension || 'html'
    		this.minimumTimeBetweenValidityChecks = this.minimumTimeBetweenValidityChecks || 1000
    	}

    	Public.create = function(app) {
    		var generatedTextResource = app.globals['com.threecrickets.prudence.GeneratedTextResource'] = {
	    		extraDocumentSources: new java.util.concurrent.CopyOnWriteArrayList(),
	    		//clientCachingMode: dynamicWebClientCachingMode,
	    		cacheKeyPatternHandlers: new java.util.concurrent.ConcurrentHashMap(),
	    		scriptletPlugins: new java.util.concurrent.ConcurrentHashMap(),
	    		passThroughDocuments: new java.util.concurrent.CopyOnWriteArraySet(),
	    		defaultIncludedName: this.defaultDocument,
	    		executionController: new com.threecrickets.prudence.util.PhpExecutionController() // Adds PHP predefined variables
    		}

    		generatedTextResource.documentSource = new com.threecrickets.scripturian.document.DocumentFileSource(this.root, this.root, this.defaultDocument, this.defaultExtenion, this.minimumTimeBetweenValidityChecks)
    		generatedTextResource.extraDocumentSources.add(new com.threecrickets.scripturian.document.DocumentFileSource(this.fragmentsRoot, this.fragmentsRoot, this.defaultDocument, this.defaultExtenion, this.minimumTimeBetweenValidityChecks))

    		//ourSettings.extraDocumentSources.add(commonFragmentsDocumentSource)
    		
    		/*for (var i in dynamicWebPassThrough) {
    			passThroughDocuments.add(dynamicWebPassThrough[i])
    		}*/
    		
    		return new org.restlet.resource.Finder(app.context, sincerity.container.dependencies.classLoader.loadClass('com.threecrickets.prudence.GeneratedTextResource'))
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.Chain
	 * @augments Prudence.Resource 
	 * @param {Array} array The array
	 */
    Public.Chain = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Chain */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = []

    	Public.create = function(app) {
    	}
    	
    	return Public
    }(Public))
	
	
    return Public
}()
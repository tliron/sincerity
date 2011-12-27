

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
    
	/**
	 * @class
	 * @name Prudence.Application
	 */
    Public.Application = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Application */
    	var Public = {}
    	
    	Public.settings = {}
    	
    	Public.routes = {}
    	
    	Public.globals = {}

    	Public.create = function(component) {
    		importClass(
    			com.threecrickets.prudence.PrudenceApplication,
    			com.threecrickets.prudence.PrudenceRouter,
    			org.restlet.routing.Template)

    		// The context
    		this.context = component.context.createChildContext()
    		
        	// The application
        	this.instance = new PrudenceApplication(this.context)
    		
    		// Descriptions
    		if (this.settings.description) {
    			Savory.Objects.merge(this.instance, this.settings.description)
    		}

        	// Attach to hosts
        	for (var name in this.settings.hosts) {
        		var host = Restlet.getHost(component, name)
        		if (!Savory.Objects.exists(host)) {
        			print('Unknown host: ' + name + '\n')
        		}
        		host.attach(this.settings.hosts[name], this.instance)
        	}

        	// Inbound root
        	this.instance.inboundRoot = new PrudenceRouter(this.context)

        	// Attach routes
        	for (var uri in this.routes) {
        		var restlet = this.routes[uri].create(this)
        		
        		var mode = null
        		if (uri[0] == '=') {
        			uri = uri.substring(1)
        			mode = Template.MODE_EQUALS
        		}
        		
        		uri = Module.cleanUri(uri)
        		print('Attaching ' + uri + ' to ' + restlet + '\n')
        		if (null !== mode) {
        			this.instance.inboundRoot.attach(uri, restlet, mode)
        		}
        		else {
        			this.instance.inboundRoot.attach(uri, restlet)
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
    		importClass(java.io.File)
    		
    		if (!(this.root instanceof File)) {
    			this.root = new File(Savory.Sincerity.here.parentFile, this.root).absoluteFile
    		}
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
    	Public._configure = ['root', 'fragmentsRoot', 'clientCachingMode', 'passThroughs', 'defaultDocument', 'defaultExtension', 'minimumTimeBetweenValidityChecks']

	    /** @ignore */
	    Public._construct = function(config) {
    		importClass(java.io.File)
    		
    		if (!(this.root instanceof File)) {
    			this.root = new File(Savory.Sincerity.here.parentFile, this.root).absoluteFile
    		}
    		if (!(this.fragmentsRoot instanceof File)) {
    			this.fragmentsRoot = new File(Savory.Sincerity.here.parentFile, this.fragmentsRoot).absoluteFile
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
    		}
    		else if (!Savory.Objects.exists(this.clientCachingMode)) {
    			this.clientCachingMode = 1
    		}

    		this.defaultDocument = this.defaultDocument || 'index'
    		this.defaultExtension = this.defaultExtension || 'html'
    		this.minimumTimeBetweenValidityChecks = this.minimumTimeBetweenValidityChecks || 1000
    	}

    	Public.create = function(app) {
    		importClass(
    			org.restlet.resource.Finder,
    			com.threecrickets.scripturian.document.DocumentFileSource,
    			com.threecrickets.prudence.util.PhpExecutionController,
    			java.util.concurrent.CopyOnWriteArrayList,
    			java.util.concurrent.CopyOnWriteArraySet,
    			java.util.concurrent.ConcurrentHashMap)

    		var generatedTextResource = app.globals['com.threecrickets.prudence.GeneratedTextResource'] = {
    			documentSource: new DocumentFileSource(this.root, this.root, this.defaultDocument, this.defaultExtenion, this.minimumTimeBetweenValidityChecks),
	    		extraDocumentSources: new CopyOnWriteArrayList(),
	    		cacheKeyPatternHandlers: new ConcurrentHashMap(),
	    		scriptletPlugins: new ConcurrentHashMap(),
	    		passThroughDocuments: new CopyOnWriteArraySet(),
	    		clientCachingMode: this.clientCachingMode,
	    		defaultIncludedName: this.defaultDocument,
	    		executionController: new PhpExecutionController() // Adds PHP predefined variables
    		}

    		generatedTextResource.extraDocumentSources.add(new DocumentFileSource(this.fragmentsRoot, this.fragmentsRoot, this.defaultDocument, this.defaultExtenion, this.minimumTimeBetweenValidityChecks))

    		//ourSettings.extraDocumentSources.add(commonFragmentsDocumentSource)
    		
    		if (this.passThroughs) {
	    		for (var i in this.passThroughs) {
	    			generatedTextResource.passThroughDocuments.add(this.passThroughs[i])
	    		}
    		}
    		
    		return new Finder(app.context, Savory.Sincerity.getClass('com.threecrickets.prudence.GeneratedTextResource'))
    	}
    	
    	return Public
    }(Public))

	/**
	 * @class
	 * @name Prudence.Resources
	 * @augments Prudence.Resources 
	 * @param {Array} array The array
	 */
    Public.Resources = Savory.Classes.define(function(Module) {
		/** @exports Public as Prudence.Resources */
    	var Public = {}
    	
	    /** @ignore */
    	Public._inherit = Module.Resource

		/** @ignore */
    	Public._configure = ['root', 'passThroughs', 'defaultDocument', 'defaultExtension', 'minimumTimeBetweenValidityChecks']

	    /** @ignore */
	    Public._construct = function(config) {
    		importClass(java.io.File)
    		
    		if (!(this.root instanceof File)) {
    			this.root = new File(Savory.Sincerity.here.parentFile, this.root).absoluteFile
    		}

    		this.defaultDocument = this.defaultDocument || 'default'
    		this.defaultExtension = this.defaultExtension || 'js'
    		this.minimumTimeBetweenValidityChecks = this.minimumTimeBetweenValidityChecks || 1000
    	}

    	Public.create = function(app) {
    		importClass(
    			org.restlet.resource.Finder,
    			com.threecrickets.scripturian.document.DocumentFileSource,
    			java.util.concurrent.CopyOnWriteArraySet)

    		var delegatedResource = app.globals['com.threecrickets.prudence.DelegatedResource'] = {
    			documentSource: new DocumentFileSource(this.root, this.root, this.defaultDocument, this.defaultExtenion, this.minimumTimeBetweenValidityChecks),
	    		passThroughDocuments: new CopyOnWriteArraySet()
    		}

    		if (this.passThroughs) {
	    		for (var i in this.passThroughs) {
	    			delegatedResource.passThroughDocuments.add(this.passThroughs[i])
	    		}
    		}
    		
    		return new Finder(app.context, Savory.Sincerity.getClass('com.threecrickets.prudence.DelegatedResource'))
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
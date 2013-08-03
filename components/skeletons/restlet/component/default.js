
//
// Sets up and starts a single Restlet component.
//
// By default, all applications under the "/applications/" subdirectory are set up,
// however you can configure a list of applications using the RESTLET_APPLICATIONS
// environment variable or the restlet.application JVM property. The value is a
// comma-delimited list of application subdirectory names.
//

document.executeOnce('/sincerity/container/')

importClass(java.lang.System)

//
// Logging
//

try {
	sincerity.run('logging:logging')
	
	// Have Restlet use the SLF4J facade (we'd get here only if the logging plugin is installed)
	var restletVersion = sincerity.container.dependencies.resolvedDependencies.getVersion('org.restlet.jse', 'restlet')
	Sincerity.Container.ensureClass('org.restlet.ext.slf4j.Slf4jLoggerFacade', ['org.restlet.jse', 'restlet-slf4j', restletVersion])
	System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade')
} catch(x) {}

// Allow for running specific applications in isolation
var applications = System.getProperty('restlet.applications')
if (!Sincerity.Objects.exists(applications)) {
	applications = System.getenv('RESTLET_APPLICATIONS')
}
if (Sincerity.Objects.exists(applications)) {
	applications = String(applications).split(',')
}

//
// Component
//

var initializers = []
var sharedGlobals = {}

// The component
var component = new org.restlet.Component()

// Assemble the component
Sincerity.Container.here = sincerity.container.getFile('component')
Sincerity.Container.executeAll('services')
Sincerity.Container.executeAll('clients')
Sincerity.Container.executeAll('servers')
Sincerity.Container.executeAll('hosts')

// Applications
if (Sincerity.Objects.exists(applications)) {
	for (var a in applications) {
		var app = applications[a]
		app = Sincerity.Container.getFileFromHere('applications', app)
		Sincerity.Container.execute(app)
	}
}
else {
	Sincerity.Container.executeAll('applications')
}

// Shared globals
sharedGlobals = Sincerity.Objects.flatten(sharedGlobals)
for (var name in sharedGlobals) {
	if (null !== sharedGlobals[name]) {
		component.context.attributes.put(name, sharedGlobals[name])
	}
}

// Start it!
component.start()

// Initializers
for (var i in initializers) {
	initializers[i]()
}

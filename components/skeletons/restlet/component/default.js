
document.executeOnce('/sincerity/container/')

importClass(java.lang.System)

//
// Logging
//

try {
sincerity.run('logging:logging')

// Have Restlet use SLF4J (we'd get here only if the logging plugin is installed)
var restletVersion = sincerity.container.dependencies.resolvedDependencies.getVersion('org.restlet.jse', 'restlet')
Sincerity.Container.ensureClass('org.restlet.ext.slf4j.Slf4jLoggerFacade', ['org.restlet.jse', 'restlet-slf4j', restletVersion])
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade')
} catch(x) {}

//
// Component
//

var initializers = []

// The component
var component = new org.restlet.Component()

// Assemble the component
Sincerity.Container.here = sincerity.container.getFile('component')
Sincerity.Container.executeAll('services')
Sincerity.Container.executeAll('clients')
Sincerity.Container.executeAll('servers')
Sincerity.Container.executeAll('hosts')
Sincerity.Container.executeAll('applications')

// Start it!
component.start()

//
// Initializers
//

for (var i in initializers) {
	initializers[i]()
}

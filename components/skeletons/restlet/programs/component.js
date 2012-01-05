
document.executeOnce('/sincerity/container/')

importClass(java.lang.System)

//
// Logging
//

try {
// This would only work if the logging plugin is installed
sincerity.run('logging:logging')

try {
	Sincerity.Container.getClass('org.restlet.ext.slf4j.Slf4jLoggerFacade')
}
catch (x) {
	// Install Restlet's SLF4J extension
	sincerity.run('dependencies:add', ['restlet.logging'])
	sincerity.run('dependencies:install')
}

// This would only work if Restlet's SLF4J extension is installed
Sincerity.Container.getClass('org.restlet.ext.slf4j.Slf4jLoggerFacade')

// Have Restlet use SLF4J
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade')
} catch(x) {}

//
// Component
//

document.execute('/component/')

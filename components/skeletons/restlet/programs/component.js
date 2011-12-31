
importClass(
	java.lang.System)

//
// Logging
//

try {
// This would only work if the logging plugin is installed
sincerity.run('logging:logging')
} catch(x) {}

try {
// This would only work if Restlet's SLF4J extension is installed
sincerity.container.dependencies.classLoader.loadClass('org.restlet.ext.slf4j.Slf4jLoggerFacade')
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade')
} catch(x) {}

//
// Component
//

document.execute('/component/')

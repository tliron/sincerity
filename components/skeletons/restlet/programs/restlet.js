
importClass(
	java.lang.System)

//
// Logging
//

try {
sincerity.run('logging:logging')
} catch(x) {}

try {
sincerity.container.classLoader.loadClass('org.restlet.ext.slf4j.Slf4jLoggerFacade')
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade')
System.out.println('SLFjjjjjjjjjj')
} catch(x) {}

//
// Component
//

document.execute('/component/')

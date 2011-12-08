
function getCommands() {
	return ['log']
}

function run(command, arguments, sincerity) {
	switch (String(command)) {
		case 'log':
			log(arguments)
			break
	}
}

function log(arguments) {
	importClass(java.lang.System, java.util.logging.LogManager, java.util.logging.Logger)

	// Configure log4j
	try {
		importClass(org.apache.log4j.PropertyConfigurator)
		basePath = container.root.path
		System.setProperty('sincerity.logs', basePath + '/logs')
		PropertyConfigurator.configure(basePath + '/configuration/logging.conf')
	} catch(x) {}

	// Remove any pre-existing configuration from JULI
	LogManager.logManager.reset()

	// Bridge JULI to SLF4J, which will in turn use log4j as its engine 
	try {
		importClass(org.slf4j.bridge.SLF4JBridgeHandler)
		SLF4JBridgeHandler.install()
	} catch(x) {}
	
	Logger.getLogger('sincerity').info(arguments[0])
}

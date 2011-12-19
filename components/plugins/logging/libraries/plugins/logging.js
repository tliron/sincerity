
importClass(
	java.lang.System,
	java.lang.ClassNotFoundException,
	java.util.logging.LogManager,
	java.util.logging.Logger,
	com.threecrickets.sincerity.exception.SincerityException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException)

function getCommands() {
	return ['logging', 'log']
}

function run(command) {
	switch (String(command.name)) {
		case 'logging':
			logging(command)
			break
		case 'log':
			log(command)
			break
	}
}

function logging(command) {
	var configurationFile = command.sincerity.container.getConfigurationFile('logging.conf')
	if (configurationFile.exists()) {
		var logsDir = command.sincerity.container.getLogsFile()
		logsDir.mkdirs()

		// The log4j configuration can use this property
		System.setProperty('sincerity.logs', logsDir)

		try {
			org.apache.log4j.xml.DOMConfigurator.configureAndWatch(configurationFile)
		}
		catch (x if x.javaException instanceof ClassNotFoundException) {
			throw new SincerityException('Could not find log4j in classpath', x.javaException)
		}
	}

	// Makes sure some servers (such as Jetty) don't log to console
	System.setProperty('java.util.logging.config.file', 'none')

	// Remove any pre-existing configuration from JULI
	LogManager.logManager.reset()

	// Bridge JULI to SLF4J, which will in turn use log4j as its engine
	try {
		org.slf4j.bridge.SLF4JBridgeHandler.install()
	}
	catch (x if x.javaException instanceof ClassNotFoundException) {
		throw new SincerityException('Could not find SLF4J bridge in classpath', x.javaException)
	}
}

function log(command) {
	if (command.arguments.length < 1) {
		throw new BadArgumentsCommandException(command, 'message')
	}

	Logger.getLogger('sincerity').info(command.arguments[0]);
}

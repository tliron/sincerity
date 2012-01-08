
document.execute('/sincerity/files/')
document.execute('/sincerity/objects/')

importClass(
	java.lang.System,
	java.lang.ClassNotFoundException,
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
	// Configure log4j
	var configurationFile = command.sincerity.container.getConfigurationFile('logging.conf')
	if (configurationFile.exists()) {
		// Configure by traditional log4j configuration file
		
		// Make sure we have a /logs/ directory
		var logsDir = command.sincerity.container.getLogsFile()
		logsDir.mkdirs()

		// The log4j configuration can use this property
		System.setProperty('sincerity.logs', logsDir)

		try {
			var contents = Sincerity.Files.loadText(configurationFile)
			if (Sincerity.Objects.startsWith(contents, '<?xml')) {
				org.apache.log4j.xml.DOMConfigurator.configureAndWatch(configurationFile)
			}
			else {
				org.apache.log4j.PropertyConfigurator.configureAndWatch(configurationFile)
			}
		}
		catch (x if x.javaException instanceof ClassNotFoundException) {
			throw new SincerityException('Could not find log4j in classpath', x.javaException)
		}
	}
	else {
		// Configure by script
		document.execute('/configuration/logging/')
	}

	// Remove any pre-existing configuration from JULI
	System.setProperty('java.util.logging.config.file', 'none')
	java.util.logging.LogManager.logManager.reset()

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

	java.util.logging.Logger.getLogger('sincerity').info(command.arguments[0]);
}
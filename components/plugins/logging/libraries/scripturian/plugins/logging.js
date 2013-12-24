
document.require(
	'/sincerity/files/',
	'/sincerity/objects/')

importClass(
	java.lang.System,
	java.lang.ClassNotFoundException,
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException)

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['logging', 'log', 'server']
}

function run(command) {
	switch (String(command.name)) {
		case 'logging':
			logging(command)
			break
		case 'log':
			log(command)
			break
		case 'server':
			server(command)
			break
	}
}

function logging(command) {
	// Configure Log4j
	var configurationFile = command.sincerity.container.getConfigurationFile('logging.conf')
	if (configurationFile.exists()) {
		// Configure by traditional Log4j configuration file
		
		// Make sure we have a /logs/ directory
		var logsDir = command.sincerity.container.getLogsFile()
		logsDir.mkdirs()

		// The Log4j configuration can use this property
		System.setProperty('sincerity.logs', logsDir)

		try {
			var contents = Sincerity.Files.loadText(configurationFile)
			var stream = new java.io.FileInputStream(configurationFile)
			try {
				var source = new org.apache.logging.log4j.core.config.ConfigurationFactory.ConfigurationSource(stream, configurationFile)
				
				var configuration
				if (Sincerity.Objects.startsWith(contents, '<?xml')) {
					configuration = org.apache.logging.log4j.core.config.XMLConfiguration(source)
				}
				else {
					configuration = org.apache.logging.log4j.core.config.JSONConfiguration(source)
				}
			}
			finally {
				stream.close()
			}
				
			new com.threecrickets.sincerity.util.ProgrammableLog4jConfigurationFactory(configuration).use()
		}
		catch (x if x.javaException instanceof ClassNotFoundException) {
			throw new CommandException(command, 'Could not find Log4j in classpath', x.javaException)
		}
	}
	else {
		// Configure by script
		document.execute('/configuration/logging/')
	}

	// Remove any pre-existing configuration from JULI
	System.setProperty('java.util.logging.config.file', 'none')
	java.util.logging.LogManager.logManager.reset()

	// Bridge JULI to SLF4J, which will in turn use Log4j as its engine
	try {
		org.slf4j.bridge.SLF4JBridgeHandler.install()
	}
	catch (x if x.javaException instanceof ClassNotFoundException) {
		throw new CommandException(command, 'Could not find SLF4J bridge in classpath', x.javaException)
	}
}

function log(command) {
	if (command.arguments.length < 1) {
		throw new BadArgumentsCommandException(command, 'message')
	}
	
	logging(command)

	java.util.logging.Logger.getLogger('sincerity').info(command.arguments[0])
}

function server(command) {
	command.parse = true
	var port = command.properties.get('port') || 4560

	logging(command)

	println('Starting Log4j server on port ' + port)

	new org.apache.logging.log4j.core.net.SocketServer(port).run()
}
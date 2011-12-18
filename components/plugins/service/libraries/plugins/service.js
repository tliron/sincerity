
function getCommands() {
	return ['service']
}

function run(command) {
	switch (String(command.name)) {
		case 'service':
			service(command)
			break
	}
}

function service(command) {
	if (command.arguments.length < 1)
		throw new com.threecrickets.sincerity.exception.BadArgumentsCommandException(command, 'uri')

	var program = command.arguments[0]
	var binary = sincerity.container.getLibrariesFile('native', 'wrapper-linux-x86-64')

	// This configuration will override anything in service.conf
	var configuration = {
		'wrapper.working.dir': sincerity.container.root,
		'wrapper.console.title': 'Sincerity',
		'wrapper.logfile': sincerity.container.getLogsFile('service.log'),
		'wrapper.java.library.path.1': sincerity.container.getLibrariesFile('native'),
		'wrapper.java.mainclass': 'org.tanukisoftware.wrapper.WrapperSimpleApp',
		'wrapper.app.parameter.1': 'com.threecrickets.sincerity.Sincerity',
		'wrapper.app.parameter.2': 'delegate:run',
		'wrapper.app.parameter.3': program
	}

	// Classpath
	var index = 1
	for (var i = sincerity.container.dependencies.getClasspaths(true).iterator(); i.hasNext(); ) {
		configuration['wrapper.java.classpath.' + index++] = i.next()
	}

	// Launch native wrapper binary
	var arguments = ['delegate:launch', binary, sincerity.container.getConfigurationFile('service.conf')]
	for (var c in configuration) {
		arguments.push(c + '=' + configuration[c])
	}
	sincerity.container.getLogsFile().mkdirs()
	sincerity.run(arguments)
}

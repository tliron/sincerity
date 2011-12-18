
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
	var arguments = command.arguments
	if (arguments.length < 1)
		throw new com.threecrickets.sincerity.exception.BadArgumentsCommandException(command, 'uri')
	var program = arguments[0]
	
	var configuration = {}
	
	sincerity.container.getLogsFile().mkdirs()

	var binary = sincerity.container.getLibrariesFile('native', 'wrapper-linux-x86-64')

	configuration['wrapper.working.dir'] = sincerity.container.root
	configuration['wrapper.console.title'] = 'Sincerity'
	configuration['wrapper.logfile'] = sincerity.container.getLogsFile('service.log')
	configuration['wrapper.java.library.path.1'] = sincerity.container.getLibrariesFile('native')
	configuration['wrapper.java.mainclass'] = 'org.tanukisoftware.wrapper.WrapperSimpleApp'
	configuration['wrapper.app.parameter.1'] = 'com.threecrickets.sincerity.Sincerity'
	configuration['wrapper.app.parameter.2'] = 'scripturian:execute'
	configuration['wrapper.app.parameter.3'] = program
	
	var index = 1
	for (var i = sincerity.container.dependencies.getClasspaths(true).iterator(); i.hasNext(); ) {
		configuration['wrapper.java.classpath.' + index++] = i.next()
	}
	
	var arguments = [binary, sincerity.container.getConfigurationFile('service.conf')]
	for (var c in configuration) {
		arguments.push(c + '=' + configuration[c])
	}
	sincerity.exec(arguments)
}

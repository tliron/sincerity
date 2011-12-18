
importClass(com.threecrickets.sincerity.exception.BadArgumentsCommandException)

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

function getPid(pidFile) {
	if (!pidFile.exists()) {
		return null
	}
	var reader = new java.io.BufferedReader(new java.io.FileReader(pidFile))
	try {
		return reader.readLine()
	}
	finally {
		reader.close()
	}
}

function kill(pid) {
	sincerity.run('delegate:launch', 'kill', pid)
}

function service(command) {
	if (command.arguments.length < 1) {
		throw new BadArgumentsCommandException(command, 'verb ("start", "stop", "run", or "status")', 'uri (for "start" or "run")')
	}

	var verb = command.arguments[0]

	var name = 'sincerity'
	var displayName = 'Sincerity'

	var pidFile = sincerity.container.getCacheFile(name + '.pid')
	
	if (verb == 'status') {
		var pid = getPid(pidFile)
		if (null === pid) {
			command.sincerity.out.println(displayName + ' is not running')
		}
		else {
			command.sincerity.out.println(displayName + ' is running (pid: ' + pid + ')')
		}
	}
	else if (verb == 'stop') {
		var pid = getPid(pidFile)
		if (null === pid) {
			command.sincerity.out.println(displayName + ' is not running')
		}
		else {
			command.sincerity.out.println('Stopping ' + displayName + ' (pid: ' + pid + ')...')
			kill(pid)
			pid = getPid(pidFile)
			if (null !== pid) {
				command.sincerity.out.println('Waiting for ' + displayName + ' to stop...')
				while (null !== pid) {
					java.lang.Thread.sleep(1000)
					pid = getPid(pidFile)
				}
			}
			command.sincerity.out.println(displayName + ' has stopped')
		}
	}
	else if ((verb == 'start') || (verb == 'run')) {
		if (command.arguments.length < 2) {
			throw new BadArgumentsCommandException(command, '"' + verb + '"', 'uri')
		}

		var binary = sincerity.container.getLibrariesFile('native', 'wrapper-linux-x86-64')
		var uri = command.arguments[1]
	
		// This configuration will override anything in service.conf
		// See: http://wrapper.tanukisoftware.com/doc/english/properties.html
		var configuration = {
			'wrapper.working.dir': sincerity.container.root,
			'wrapper.pidfile': pidFile,
			'wrapper.name': name,
			'wrapper.displayname': displayName,
			'wrapper.ntservice.name': name,
			'wrapper.ntservice.displayname': displayName,
			'wrapper.console.title': displayName,
			'wrapper.syslog.ident': name,
			'wrapper.logfile': sincerity.container.getLogsFile('service.log'),
			'wrapper.java.library.path.1': sincerity.container.getLibrariesFile('native'),
			'wrapper.java.mainclass': 'org.tanukisoftware.wrapper.WrapperSimpleApp',
			'wrapper.app.parameter.1': 'com.threecrickets.sincerity.Sincerity',
			'wrapper.app.parameter.2': 'delegate:run',
			'wrapper.app.parameter.3': uri
		}
		
		// Classpath
		var index = 1
		for (var i = sincerity.container.dependencies.getClasspaths(true).iterator(); i.hasNext(); ) {
			configuration['wrapper.java.classpath.' + index++] = i.next()
		}

		// Daemonize?
		if (verb == 'start') {
			configuration['wrapper.daemonize'] = 'TRUE'
		}

		// Launch native wrapper binary
		var arguments = [binary, sincerity.container.getConfigurationFile('service.conf')]
		for (var c in configuration) {
			arguments.push(c + '=' + configuration[c])
		}
		sincerity.container.getLogsFile().mkdirs()
		
		if (verb == 'run') {
			command.sincerity.out.println('Running ' + displayName + '...')
		}
		sincerity.run('delegate:launch', arguments)
		if (verb == 'start') {
			command.sincerity.out.println('Started ' + displayName)
		}
	}
	else {
		throw new BadArgumentsCommandException(command, 'verb ("start", "stop", "run", or "status")', 'uri (for "start" or "run")')
	}
}

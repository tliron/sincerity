
importClass(
	com.threecrickets.sincerity.exception.BadArgumentsCommandException,
	com.threecrickets.sincerity.exception.SincerityException,
	java.io.File,
	java.io.BufferedReader,
	java.io.FileReader,
	java.lang.System)

var os = getOs()

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
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'uri', 'verb ("start", "stop", "restart", "run", or "status")')
	}

	var uri = command.arguments[0]
	var verb = command.arguments[1]

	var name = uri.replace('/', '_')
	var displayName = name

	var cacheDir = sincerity.container.getCacheFile('service')
	cacheDir.mkdirs()
	
	var pidFile = new File(cacheDir, name + '.pid')
	var statusFile = new File(cacheDir, name + '.status')
	
	if (verb == 'status') {
		var status = getStatus(statusFile)
		if (null === status) {
			command.sincerity.out.println(displayName + ' is not running')
		}
		else {
			var pid = null
			if (isRunning(status)) {
				pid = getPid(pidFile)
			}
			command.sincerity.out.println(displayName + ': ' + status + (null === pid ? '' : ' (pid: ' + pid + ')'))
		}
		return
	}
	
	if ((verb == 'stop' ) || (verb == 'restart')) {
		var status = getStatus(statusFile)
		var pid = getPid(pidFile)
		if (isStopped(status) || (null === pid)) {
			command.sincerity.out.println(displayName + ' is not running' + (null === status ? '' : ' (' + status + ')'))
			return
		}
		
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
		if (verb == 'stop') {
			return
		}
	}
	
	if ((verb == 'start') || (verb == 'restart') || (verb == 'run')) {
		var status = getStatus(statusFile)
		if (isRunning(status)) {
			command.sincerity.out.println(displayName + ' is already running (' + status + ')')
			return
		}

		var binary = 'wrapper-' + os.name + '-' + os.architecture + '-' + os.bits
		binary = sincerity.container.getLibrariesFile('native', binary)
		if (!binary.exists()) {
			if (isSupported(os)) {
				sincerity.run('dependencies:add', ['com.tanukisoftware', 'wrapper-' + os.name, '3.5.13'])
				sincerity.run('dependencies:install')
			}
			if (!binary.exists()) {
				throw new SincerityException('The service plugin in this container does not support your operating system: ' + os.name + ', ' + os.architecture + ', ' + os.bits)
			}
		}
	
		// This configuration will override anything in service.conf
		// See: http://wrapper.tanukisoftware.com/doc/english/properties.html
		var configuration = {
			'wrapper.working.dir': sincerity.container.root,
			'wrapper.pidfile': pidFile,
			'wrapper.java.statusfile': statusFile,
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
			'wrapper.app.parameter.2': 'delegate:start',
			'wrapper.app.parameter.3': uri
		}
		
		// JVM switches
		var jvmDir = sincerity.container.getConfigurationFile('service', 'jvm')
		var index = 1
		if (jvmDir.directory) {
			var files = jvmDir.listFiles()
			for (var f in files) {
				var file = files[f]
				if (file.name.endsWith('.conf')) {
					var reader = new BufferedReader(new FileReader(file))
					try {
						while (null !== (line = reader.readLine())) {
							if ((line.length() == 0) || line.startsWith('#')) {
								continue
							}
							configuration['wrapper.java.additional.' + index++] = line
						}
					}
					finally {
						reader.close()
					}
				}
			}
		}
		
		// Classpath
		index = 1
		for (var i = sincerity.container.dependencies.getClasspaths(true).iterator(); i.hasNext(); ) {
			configuration['wrapper.java.classpath.' + index++] = i.next()
		}

		// Daemonize?
		if (verb == 'start') {
			configuration['wrapper.daemonize'] = 'TRUE'
		}

		// Assemble arguments
		var arguments = [binary, sincerity.container.getConfigurationFile('service', 'service.conf')]
		for (var c in configuration) {
			arguments.push(c + '=' + configuration[c])
		}
		sincerity.container.getLogsFile().mkdirs()

		/*for (c in arguments) {
			command.sincerity.out.println(c + ' = ' + arguments[c])
		}*/

		// Launch native wrapper binary
		if (verb == 'run') {
			command.sincerity.out.println('Running ' + displayName + '...')
		}
		sincerity.run('delegate:execute', arguments)
		if (verb == 'start') {
			command.sincerity.out.println('Started ' + displayName)
		}
		return
	}

	throw new BadArgumentsCommandException(command, 'uri', 'verb ("start", "stop", "restart", "run", or "status")')
}

function getOs() {
	// See: http://lopica.sourceforge.net/os.html
	var name = System.getProperty('os.name')
	var architecture = System.getProperty('os.arch')
	var bits = System.getProperty('sun.arch.data.model')
	
	if (name == 'AIX') {
		name = 'aix'
		architecture = 'ppc'
	}
	else if (name == 'FreeBSD') {
		name = 'freebsd'
		architecture = 'x86'
	}
	else if (name == 'HP-UX') {
		name = 'hpux'
		if (architecture.indexOf('RISC') != -1) {
			architecture = 'parisc'
		}
		else {
			architecture = 'ia'
		}
	}
	else if (name == 'Linux') {
		name = 'linux'
		if ((architecture.indexOf('86') != -1) || (architecture.indexOf('amd') != -1)) {
			architecture = 'x86'
		}
		else if (architecture.indexOf('ppc') != -1) {
			architecture = 'ppc'
		}
		else {
			architecture = 'ia'
		}
	}
	else if (name == 'Mac OS X') {
		name = 'macosx'
		architecture = 'universal'
	}
	else if ((name == 'Solaris') || (name == 'SunOS')) {
		name = 'solaris'
		if ((architecture.indexOf('86') != -1) || (architecture.indexOf('amd') != -1)) {
			architecture = 'x86'
		}
		else {
			architecture = 'sparc'
		}
	}

	return {
		name: name,
		architecture: architecture,
		bits: bits
	}
}

function getPid(pidFile) {
	if (!pidFile.exists()) {
		return null
	}
	var reader = new BufferedReader(new FileReader(pidFile))
	try {
		return reader.readLine()
	}
	finally {
		reader.close()
	}
}

function getStatus(statusFile) {
	if (!statusFile.exists()) {
		return null
	}
	var reader = new BufferedReader(new FileReader(statusFile))
	try {
		return reader.readLine()
	}
	finally {
		reader.close()
	}
}

function isRunning(status) {
	return (status == 'LAUNCH(DELAY)') || (status == 'LAUNCHING') || (status == 'LAUNCHED') || (status == 'STARTING') || (status == 'STARTED')	
}

function isStopped(status) {
	return (null === status) || (status == 'DOWN') || (status == 'DOWN_CLEAN') || (status == 'STOPPING') || (status == 'STOPPED')
}

function isSupported(os) {
	return (os.name == 'aix') || (os.name == 'freebsd') || (os.name == 'hpux') || (os.name == 'linux') || (os.name == 'macosx') || (os.name == 'solaris') || (os.name == 'windows')
}

function kill(pid) {
	if (os.name == 'Windows') {
		// TODO
	}
	else {
		sincerity.run('delegate:execute', 'kill', pid)
	}
}

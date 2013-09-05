
document.executeOnce('/sincerity/files/')
document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/objects/')

importClass(
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException,
	java.io.File,
	java.io.BufferedReader,
	java.io.FileReader,
	java.lang.System)

var os = getOs()

function getInterfaceVersion() {
	return 1
}

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
	command.parse = true
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'uri', 'verb ("start", "stop", "restart", "console", or "status")')
	}

	var uri = command.arguments[0]
	var verb = command.arguments[1]
	var verbose = command.switches.contains('verbose')

	var name = uri.replace('/', '_')
	var displayName = name

	var cacheDir = command.sincerity.container.getCacheFile('service')
	cacheDir.mkdirs()
	
	var pidFile = new File(cacheDir, name + '.pid')
	var statusFile = new File(cacheDir, name + '.status')
	
	if (verb == 'status') {
		var pid = getPid(pidFile, statusFile)
		var status = getStatus(statusFile)
		if (null === status) {
			command.sincerity.out.println(displayName + ' is not running')
		}
		else {
			command.sincerity.out.println(displayName + ': ' + status + (null === pid ? '' : ' (pid: ' + pid + ')'))
		}
		return
	}
	
	if ((verb == 'stop' ) || (verb == 'restart')) {
		var pid = getPid(pidFile, statusFile)
		var status = getStatus(statusFile)
		if (isStopped(status) || (null === pid)) {
			command.sincerity.out.println(displayName + ' is not running' + (null === status ? '' : ' (' + status + ')'))
			return
		}
		
		command.sincerity.out.println('Stopping ' + displayName + ' (pid: ' + pid + ')...')
		Sincerity.JVM.kill(pid)
		pid = getPid(pidFile, statusFile)
		if (null !== pid) {
			command.sincerity.out.println('Waiting for ' + displayName + ' to stop...')
			while (null !== pid) {
				java.lang.Thread.sleep(1000)
				pid = getPid(pidFile, statusFile)
			}
		}
		command.sincerity.out.println(displayName + ' has stopped')
		if (verb == 'stop') {
			return
		}
	}
	
	if ((verb == 'start') || (verb == 'restart') || (verb == 'console')) {
		var pid = getPid(pidFile, statusFile)
		var status = getStatus(statusFile)
		if (isRunning(status)) {
			command.sincerity.out.println(displayName + ' is already running (' + status + ')')
			return
		}

		var binary = 'wrapper-' + os.name + '-' + os.architecture + '-' + os.bits
		binary = command.sincerity.container.getLibrariesFile('native', binary)
		if (!binary.exists()) {
			if (isSupported(os)) {
				var version = command.sincerity.container.dependencies.resolvedDependencies.getVersion('com.tanukisoftware', 'wrapper-base')
				command.sincerity.run('dependencies:add', ['com.tanukisoftware', 'wrapper-' + os.name, version])
				command.sincerity.run('artifacts:install')
			}
			if (!binary.exists()) {
				throw new CommandException(command, 'The service plugin in this container does not support your operating system: ' + os.name + ', ' + os.architecture + ', ' + os.bits)
			}
		}
		
		// Make sure our native executables are executable
		var nativeDir = command.sincerity.container.getLibrariesFile('native')
		if (nativeDir.directory) {
			var files = nativeDir.listFiles()
			for (var f in files) {
				var file = files[f]
				if (file.name.startsWith('wrapper-')) {
					Sincerity.Files.makeExecutable(file)
				}
			}
		}
	
		// This configuration will override anything in service.conf
		// See: http://wrapper.tanukisoftware.com/doc/english/properties.html
		var configuration = {
			wrapper: {
				name: name,
				displayname: displayName,
				pidfile: pidFile,
				'pidfile.strict': true,
				logfile: command.sincerity.container.getLogsFile('service-' + name + '.log'),
				working: {
					dir: command.sincerity.container.root
				},
				ntservice: {
					name: name,
					displayname: displayName
				},
				console: {
					title: displayName
				},
				sysLog: {
					ident: name
				},
				java: {
					statusfile: statusFile,
					mainclass: 'org.tanukisoftware.wrapper.WrapperSimpleApp',
					library: {
						'path.1': command.sincerity.container.getLibrariesFile('native')
					}
				},
				app: {
					'parameter.1': 'com.threecrickets.sincerity.Sincerity',
					'parameter.2': 'delegate:start',
					'parameter.3': uri
				}
			}
		}
		
		// JVM switches
		var jvmDir = command.sincerity.container.getConfigurationFile('service', 'jvm')
		var index = 1
		configuration.wrapper.java['additional.' + index++] = '-Dsincerity.home=' + command.sincerity.home
		configuration.wrapper.java['additional.' + index++] = '-Dsincerity.container=' + command.sincerity.container.root
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
							configuration.wrapper.java['additional.' + index++] = line
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
		for (var i = command.sincerity.container.dependencies.getClasspaths(true).iterator(); i.hasNext(); ) {
			configuration.wrapper.java['classpath.' + index++] = i.next()
		}

		// Daemonize?
		if ((verb == 'start') || (verb == 'restart')) {
			configuration.wrapper.daemonize = 'TRUE'
		}

		// Assemble arguments
		var arguments = [binary, command.sincerity.container.getConfigurationFile('service', 'service.conf')]
		configuration = Sincerity.Objects.flatten(configuration)
		for (var c in configuration) {
			arguments.push(c + '=' + configuration[c])
		}
		command.sincerity.container.getLogsFile().mkdirs()

		if (verbose) {
			command.sincerity.out.println('Arguments:')
			for (c in arguments) {
				command.sincerity.out.println(' ' + c + ' = ' + arguments[c])
			}
		}

		// Launch native wrapper binary
		if (verb == 'console') {
			command.sincerity.out.println('Running ' + displayName + '...')
		}
		command.sincerity.run('delegate:execute', arguments)
		if ((verb == 'start') || (verb == 'restart')) {
			command.sincerity.out.println('Started ' + displayName)
		}
		return
	}

	throw new BadArgumentsCommandException(command, 'uri', 'verb ("start", "stop", "restart", "console", or "status")')
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
		else if (architecture.indexOf('arm') != -1) {
			// TODO: the JVM has no way to distinguish between armhf and armel :(
			architecture = 'armhf'
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
	
	// TODO: Windows

	return {
		name: name,
		architecture: architecture,
		bits: bits
	}
}

function getPid(pidFile, statusFile) {
	var pid = pidFile.exists() ? String(Sincerity.Files.loadText(pidFile)).trim() : null
	if ((null !== pid) && Sincerity.Objects.exists(statusFile)) {
		var state = Sincerity.JVM.getProcessState(pid)
		if (state === false) {
			pidFile['delete']()
			statusFile['delete']()
			pid = null
		}
	}
	return pid
}

function getStatus(statusFile) {
	return statusFile.exists() ? String(Sincerity.Files.loadText(statusFile)).trim() : null
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

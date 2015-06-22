
document.require('/sincerity/jvm/')

var MAIN_CLASS = 'org.python.util.jython'

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['python', 'easy_install']
}

function run(command) {
	switch (String(command.name)) {
		case 'python':
			python(command)
			break
		case 'easy_install':
			easy_install(command)
			break
	}
}

function python(command) {
	importClass(
			org.python.core.Py,
			org.python.core.PyString,
			org.python.core.Options,
			java.lang.System)

	// The Python standard library is here (Jython expects a "Lib" subdirectory underneath)
	System.setProperty('python.home', command.sincerity.container.getLibrariesFile('python'))

	// The cachedir must be absolute or relative to PYTHON_HOME (Jython will add a "packages" subdirectory to it)
	System.setProperty('python.cachedir', command.sincerity.container.getCacheFile('python'))

	// Reduce default verbosity (otherwise we get annoying "processing new jar" messages)
	Options.verbose = Py.WARNING

	// This is Jython's 'sys' module (a singleton)
	var sys = Py.systemState

	sys.exec_prefix = new PyString(String(command.sincerity.container.getExecutablesFile()))
	sys.executable = new PyString(String(command.sincerity.container.getExecutablesFile('python')))
	
	// Put eggs into sys.path
	/*var eggsDir = command.sincerity.container.getLibrariesFile('eggs')
	if (eggsDir.directory) {
		var files = eggsDir.listFiles()
		for (var i in files) {
			var file = files[i]
			if (file.name.endsWith('.egg')) {
				sys.path.add(String(file))
			}
		}
	}*/

	// The Jython runtime does not reinitialize the 'sys' module singleton if it's already initialized,
	// so we must explicitly set sys.argv if we want to run it more than once with different arguments
	sys.argv.clear()

	var runArguments = ['delegate:main', MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
		sys.argv.add(new PyString(arguments[i]))
	}
	command.sincerity.run(Sincerity.JVM.toArray(runArguments, 'java.lang.String'))
}

function easy_install(command) {
	// We are executing easy_install in a separate process, because otherwise it will exit our process when done :(
	var runArguments = ['delegate:execute', 'easy_install']
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	command.sincerity.run(Sincerity.JVM.toArray(runArguments, 'java.lang.String'))
}

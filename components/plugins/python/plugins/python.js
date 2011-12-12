
importClass(java.lang.System, java.io.File)

var MAIN_CLASS = 'org.python.util.jython'

function getCommands() {
	return ['python']
}

function run(command) {
	switch (String(command.name)) {
		case 'python':
			python(command)
			break
	}
}

function python(command) {
	var root = command.sincerity.container.root
	
	// The Python standard library is here
	System.setProperty('python.home', new File(root, 'libraries/python').canonicalPath)

	// The cachedir must be absolute or relative to PYTHON_HOME (Jython will add a "packages" subdirectory to it)
	System.setProperty('python.cachedir', new File(root, 'cache/python').canonicalPath)

	// Reduce default verbosity (otherwise we get annoying "processing new jar" messages)
	org.python.core.Options.verbose = org.python.core.Py.WARNING

	var sys = org.python.core.Py.systemState

	// sys.executable is used to spawn Python subprocesses
	sys.executable = new org.python.core.PyString(new File(root, 'programs/python').canonicalPath)
	
	// Put eggs into sys.path
	var eggsDir = new File(root, 'libraries/python')
	if (eggsDir.exists()) {
		var files = eggsDir.listFiles()
		for (var i in files) {
			var file = files[i]
			if (file.name.endsWith('.egg')) {
				sys.path.add(String(file))
			}
		}
	}
	
	// Jython does not initialize its state more than once, so we must explicitly
	// set sys.argv if we want to run it more than once with different arguments

	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
    sys.argv.clear()
	for (var i in arguments) {
		mainArguments.push(arguments[i])
		sys.argv.add(arguments[i])
	}
	command.sincerity.run('main:main', mainArguments)
}

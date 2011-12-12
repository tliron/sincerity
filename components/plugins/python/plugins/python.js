
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
	// python.cachedir must be absolute or relative to PYTHON_HOME; Jython will add a "packages" subdirectory to it
	
	var root = command.sincerity.container.root
	System.setProperty('python.home', '/Depot/Projects/Collaborative/Prudence/libraries/jython/lib')
	System.setProperty('python.cachedir', new File(root, 'cache/python').canonicalPath)

	var sys = org.python.core.Py.systemState

	sys.executable = new org.python.core.PyString(new File(root, 'programs/python').canonicalPath)
	
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
			
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
    sys.argv.clear()
	for (var i in arguments) {
		mainArguments.push(arguments[i])
		sys.argv.add(arguments[i])
	}
	command.sincerity.run('main:main', mainArguments)
}

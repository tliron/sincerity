
importClass(java.lang.System, java.io.File)

var MAIN_CLASS = 'org.python.util.jython'

function getCommands() {
	return ['python', 'validate']
}

function run(command) {
	switch (String(command.name)) {
		case 'python':
			python(command)
			break
	}
}

function python(command) {
	// Notes:
	//
	// python.cachedir:   Must be absolute or relative to PYTHON_HOME;
	//                    Jython will add a "packages" subdirectory to it
	// python.executable: Used by install
	
	var root = command.sincerity.container.root
	
	System.setProperty('python.home', '/Depot/Projects/Collaborative/Prudence/libraries/jython/lib')
	System.setProperty('python.cachedir', new File(root, 'cache/python').canonicalPath)
	System.setProperty('python.executable', new File(root, 'programs/python').canonicalPath)
			
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var length = arguments.length, i = 0; i < length; i++) {
		mainArguments.push(arguments[i])
	}
	command.sincerity.run('main:main', mainArguments)
}

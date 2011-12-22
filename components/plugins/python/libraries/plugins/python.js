
importClass(
	org.python.core.Py,
	org.python.core.PyString,
	org.python.core.Options,
	java.lang.System,
	java.io.File)

var MAIN_CLASS = 'org.python.util.jython'

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
	// The Python standard library is here
	var pythonHome = command.sincerity.container.getLibrariesFile('python')
	System.setProperty('python.home', pythonHome)

	// The cachedir must be absolute or relative to PYTHON_HOME (Jython will add a "packages" subdirectory to it)
	System.setProperty('python.cachedir', command.sincerity.container.getCacheFile('python'))

	// Reduce default verbosity (otherwise we get annoying "processing new jar" messages)
	Options.verbose = Py.WARNING

	// This is Jython's 'sys' module (a singleton)
	var sys = Py.systemState

	// sys.executable is used to spawn Python subprocesses
	sys.executable = new PyString(command.sincerity.container.getExecutablesFile('python'))
	//sys.exec_prefix = new org.python.core.PyString(new File(root, 'programs').canonicalPath)
	
	// Put eggs into sys.path
	var files = pythonHome.listFiles()
	for (var i in files) {
		var file = files[i]
		if (file.name.endsWith('.egg')) {
			sys.path.add(String(file))
		}
	}
	
	// The Jython runtime does not reinitialize the 'sys' module singleton if its already initialized,
	// so we must explicitly set sys.argv if we want to run it more than once with different arguments
	sys.argv.clear()

	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var i in command.arguments) {
		mainArguments.push(command.arguments[i])
		sys.argv.add(command.arguments[i])
	}

	command.sincerity.run('delegate:main', mainArguments)
}

function easy_install(command) {
	var sitePackages = command.sincerity.container.getLibrariesFile('python', 'Lib', 'site-packages')
	sitePackages.mkdirs()
	var egg = command.sincerity.container.getLibrariesFile('python', 'setuptools-0.6c11-py2.5.egg')
	command.sincerity.run('python:python', ['-c', "" +
	"egg = '/home/emblemparade/xxx/libraries/python/setuptools-0.6c11-py2.5.egg';" +
	"from setuptools.command.easy_install import main;" +                           
	"import sys;" +
	"sys.argv=['easy_install', egg];" +
	"main();"])
}

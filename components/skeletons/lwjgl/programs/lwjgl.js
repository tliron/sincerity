
importClass(java.lang.System)

function validate() {
	// See: http://lopica.sourceforge.net/os.html
	var os = System.getProperty('os.name')
	if (os == 'Linux') {
		os = 'linux'
	}
	else if (os == 'Mac OS X') {
		os = 'macosx'
	}
	else if ((os == 'Solaris') || (os == 'SunOS')) {
		os = 'solaris'
	}
	else if (os == 'Windows') {
		os = 'windows'
	}
	else {
		println('Your operating system (' + os + ') is not supported by lwjgl.')
		System.exit(1)
	}
	
	var module = 'lwjgl-' + os
	if (!sincerity.container.dependencies.has('org.lwjgl', module)) {
		var version = sincerity.container.dependencies.resolvedDependencies.getVersion('org.lwjgl', 'lwjgl')
		sincerity.run('dependencies:add', ['org.lwjgl', module, version])
		sincerity.run('dependencies:install')
	}
}

validate()
var arguments = ['game']
for (var i = 1, length = application.arguments.length; i < length; i++) {
	arguments.push(application.arguments[i])
}
sincerity.run('delegate:start', arguments)

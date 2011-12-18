
var binary = sincerity.container.getLibrariesFile('native', 'wrapper-linux-x86-64')


var binary = sincerity.container.getLibrariesFile('native', 'wrapper-linux-x86-64')
if (undefined !== binary.executable) {
	// JVM6+
	binary.executable = true
}
else {
	sincerity.exec(['chmod', '+x', binary])
}


var configuration = {}

sincerity.container.getLogsFile().mkdirs()

configuration['wrapper.working.dir'] = sincerity.container.root
configuration['wrapper.console.title'] = 'Sincerity'
configuration['wrapper.logfile'] = sincerity.container.getLogsFile('service.log')
configuration['wrapper.java.library.path.1'] = sincerity.container.getLibrariesFile('native')
configuration['wrapper.java.mainclass'] = 'org.tanukisoftware.wrapper.WrapperSimpleApp'
configuration['wrapper.app.parameter.1'] = 'com.threecrickets.sincerity.Sincerity'
configuration['wrapper.app.parameter.2'] = 'scripturian:execute'
configuration['wrapper.app.parameter.3'] = '/programs/web/'

var index = 1
for (var i = sincerity.container.dependencies.getClasspaths(true).iterator(); i.hasNext(); ) {
	configuration['wrapper.java.classpath.' + index++] = i.next()
}

function console() {
	var arguments = [binary, sincerity.container.getConfigurationFile('service.conf')]
	for (var c in configuration) {
		arguments.push(c + '=' + configuration[c])
	}
	sincerity.exec(arguments)
}

console()

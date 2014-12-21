
document.require('/sincerity/jvm/')

var MAIN_CLASS = 'org.apache.felix.main.Main'

importClass(java.lang.System)

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['felix', 'gogo']
}

function run(command) {
	switch (String(command.name)) {
		case 'felix':
		case 'gogo':
			gogo(command)
			break
	}
}

function gogo(command) {
	// These can be used in felix.conf
	System.setProperty('sincerity.cache', command.sincerity.container.getCacheFile('felix'))
	System.setProperty('sincerity.autodeploy', command.sincerity.container.getLibrariesFile('bootstrap'))
	
	System.setProperty('felix.config.properties', command.sincerity.container.getConfigurationFile('felix.conf').toURL())
	
	var runArguments = ['delegate:main', MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	command.sincerity.run(Sincerity.JVM.toArray(runArguments, 'java.lang.String'))
}

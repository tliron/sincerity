
document.require('/sincerity/jvm/')

var MAIN_CLASS = 'groovy.ui.GroovyMain'

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['groovy']
}

function run(command) {
	switch (String(command.name)) {
		case 'groovy':
			groovy(command)
			break
	}
}

function groovy(command) {
	var runArguments = ['delegate:main', MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	command.sincerity.run(Sincerity.JVM.toArray(runArguments, 'java.lang.String'))
}

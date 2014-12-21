
document.require(
	'/sincerity/platform/',
	'/sincerity/jvm/')

var MAIN_CLASS = Sincerity.Platform.isRhino ? 'org.mozilla.javascript.tools.shell.Main' : 'jdk.nashorn.tools.Shell'

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['javascript']
}

function run(command) {
	switch (String(command.name)) {
		case 'javascript':
			javascript(command)
			break
	}
}

function javascript(command) {
	var runArguments = ['delegate:main', MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	command.sincerity.run(Sincerity.JVM.toArray(runArguments, 'java.lang.String'))
}

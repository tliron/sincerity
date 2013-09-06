
var MAIN_CLASS = 'org.mozilla.javascript.tools.shell.Main'

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
	command.sincerity.run(runArguments)
}

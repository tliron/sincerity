
var MAIN_CLASS = 'jdk.nashorn.tools.Shell'

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['javascript-nashorn']
}

function run(command) {
	switch (String(command.name)) {
		case 'javascript-nashorn':
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

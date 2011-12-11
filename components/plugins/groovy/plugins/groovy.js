
var MAIN_CLASS = 'groovy.ui.GroovyMain'

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
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var length = arguments.length, i = 0; i < length; i++) {
		mainArguments.push(arguments[i])
	}
	command.sincerity.run('main:main', mainArguments)
}

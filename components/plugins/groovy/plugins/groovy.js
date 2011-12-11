
var GROUP = 'org.codehaus.groovy'
var NAME = 'groovy'
var VERSION = '1.8.1'
var MAIN_CLASS = 'groovy.ui.GroovyMain'

function getCommands() {
	return ['groovy', 'validate']
}

function run(command) {
	switch (String(command.name)) {
		case 'groovy':
			groovy(command)
			break
		case 'validate':
			validate(command.sincerity)
			break
	}
}

function groovy(command) {
	validate(command.sincerity)
	
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var length = arguments.length, i = 0; i < length; i++) {
		mainArguments.push(arguments[i])
	}
	command.sincerity.run('main:main', mainArguments)
}

function validate(sincerity) {
	var dependencies = sincerity.container.dependencies
	if (!dependencies.has(GROUP, NAME, VERSION)) {
		dependencies.add(GROUP, NAME, VERSION)
		dependencies.install(false)
	}
}

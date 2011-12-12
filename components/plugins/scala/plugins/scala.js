
var MAIN_CLASS = 'scala.tools.nsc.MainGenericRunner'

function getCommands() {
	return ['scala']
}

function run(command) {
	switch (String(command.name)) {
		case 'scala':
			scala(command)
			break
	}
}

function scala(command) {
	var mainArguments = [MAIN_CLASS, '-usejavacp']
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}
	command.sincerity.run('main:main', mainArguments)
}

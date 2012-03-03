
var MAIN_CLASS = 'render.main'

function getCommands() {
	return ['render']
}

function run(command) {
	switch (String(command.name)) {
		case 'render':
			render(command)
			break
	}
}

function render(command) {
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}
	command.sincerity.run('delegate:main', mainArguments)
}

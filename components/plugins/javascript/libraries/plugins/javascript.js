
var MAIN_CLASS = 'org.mozilla.javascript.tools.shell.Main'

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
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}
	command.sincerity.run('main:main', mainArguments)
}

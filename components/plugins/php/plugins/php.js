
var MAIN_CLASS = 'com.caucho.quercus.Quercus'

function getCommands() {
	return ['php']
}

function run(command) {
	switch (String(command.name)) {
		case 'php':
			php(command)
			break
	}
}

function php(command) {
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}
	if (mainArguments.length == 1) {
		mainArguments.push('--help')
	}
	command.sincerity.run('main:main', mainArguments)
}

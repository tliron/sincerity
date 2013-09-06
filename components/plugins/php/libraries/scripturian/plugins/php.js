
var MAIN_CLASS = 'com.caucho.quercus.Quercus'

function getInterfaceVersion() {
	return 1
}

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
	var runArguments = ['delegate:main', MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	if (runArguments.length == 2) {
		// Quercus throws an exception if it gets no arguments
		runArguments.push('-h')
	}
	command.sincerity.run(runArguments)
}


function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['rails']
}

function run(command) {
	switch (String(command.name)) {
		case 'rails':
			rails(command)
			break
	}
}

function rails(command) {
	var runArguments = ['ruby:ruby', '-C' + command.sincerity.container.getFile('app'), 'bin/rails']
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	command.sincerity.run(runArguments)
}

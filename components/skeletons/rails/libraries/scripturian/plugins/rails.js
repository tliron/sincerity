
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
	var rubyArguments = ['-C' + command.sincerity.container.getFile('app'), command.sincerity.container.getExecutablesFile('rails')]
	var arguments = command.arguments
	for (var i in arguments) {
		rubyArguments.push(arguments[i])
	}
	
	command.sincerity.run('ruby:ruby', rubyArguments)
}

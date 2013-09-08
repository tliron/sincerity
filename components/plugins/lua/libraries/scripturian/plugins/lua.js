
function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['lua', 'luac', 'luajc']
}

function run(command) {
	var runArguments = ['delegate:main', command.name]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	command.sincerity.run(runArguments)
}

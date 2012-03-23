
function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['jsdoc']
}

function run(command) {
	switch (String(command.name)) {
		case 'jsdoc':
			jsdoc(command)
			break
	}
}

function jsdoc(command) {
	var run = command.sincerity.container.getLibrariesFile('javascript', 'jsdoc-toolkit', 'app', 'run.js')

	var mainArguments = [run]
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}
	mainArguments.push('-j=' + run)
	command.sincerity.run('javascript:javascript', mainArguments)
}

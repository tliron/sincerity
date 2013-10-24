
document.require('/sincerity/platform/')

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
	var run = String(command.sincerity.container.getLibrariesFile('javascript', 'jsdoc-toolkit', 'app', 'run.js'))

	var runArguments = ['javascript:javascript', run]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	if (Sincerity.Platform.isRhino) {
		runArguments.push('-j=' + run)
	}
	command.sincerity.run(runArguments)
}

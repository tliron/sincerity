
function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['manage']
}

function run(command) {
	switch (String(command.name)) {
		case 'manage':
			manage(command)
			break
	}
}

function manage(command) {
	var manageFile = sincerity.container.getFile('project', 'manage.py')
	var runArguments = ['python:python', manageFile]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	sincerity.run(runArguments)
}

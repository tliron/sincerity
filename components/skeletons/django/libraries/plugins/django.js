
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
	var pythonArguments = [manageFile]
	var arguments = command.arguments
	for (var i in arguments) {
		pythonArguments.push(arguments[i])
	}
	sincerity.run('python:python', pythonArguments)
}


var MAIN_CLASS = 'clojure.main'

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['clojure']
}

function run(command) {
	switch (String(command.name)) {
		case 'clojure':
			clojure(command)
			break
	}
}

function clojure(command) {
	var runArguments = ['delegate:main', MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	command.sincerity.run(runArguments)
}

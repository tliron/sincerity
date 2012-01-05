
var MAIN_CLASS = 'clojure.main'

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
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}
	command.sincerity.run('delegate:main', mainArguments)
}

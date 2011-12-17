
var MAIN_CLASS = 'org.jruby.Main'

function getCommands() {
	return ['ruby']
}

function run(command) {
	switch (String(command.name)) {
		case 'ruby':
			ruby(command)
			break
	}
}

function ruby(command) {
	var mainArguments = [MAIN_CLASS]
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}
	command.sincerity.run('main:main', mainArguments)
}

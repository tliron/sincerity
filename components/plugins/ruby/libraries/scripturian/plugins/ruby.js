
importClass(java.lang.System)

var MAIN_CLASS = 'org.jruby.Main'

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['ruby', 'gem', 'ast', 'irb', 'rake', 'rdoc', 'ri', 'testrb']
}

function run(command) {
	switch (String(command.name)) {
		case 'ruby':
			ruby(command)
			break
		case 'gem':
			gem(command)
			break
		default:
			ruby(command, [sincerity.container.getExecutablesFile(command.name)])
			break
	}
}

function ruby(command, preArguments, postArguments) {
	System.setProperty('jruby.home', command.sincerity.container.getLibrariesFile('ruby'))

	// The Ruby standard library is here
	System.setProperty('jruby.lib', command.sincerity.container.getLibrariesFile('ruby', 'lib'))

	// JFFI
	System.setProperty('jffi.boot.library.path', command.sincerity.container.getLibrariesFile('ruby', 'native'))
	
	var runArguments = ['delegate:main', MAIN_CLASS]
	if (preArguments) {
		for (var i in preArguments) {
			runArguments.push(preArguments[i])
		}
	}
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	if (postArguments) {
		for (var i in postArguments) {
			runArguments.push(postArguments[i])
		}
	}
	
	command.sincerity.run(runArguments)
}

function gem(command) {
	// We are executing gem in a separate process, because otherwise it will exit our process when done :(
	var runArguments = ['delegate:execute', 'gem']
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
		if ((i == 0) && (arguments[i] == 'install')) {
			runArguments.push('--bindir', sincerity.container.getExecutablesFile())
		}
	}
	sincerity.run(runArguments)
}

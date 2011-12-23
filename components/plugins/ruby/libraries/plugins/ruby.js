
importClass(java.lang.System)

var MAIN_CLASS = 'org.jruby.Main'

function getCommands() {
	return ['ruby', 'rubyc', 'irb', 'gem', 'rdoc', 'ri', 'rake', 'testrb']
}

function run(command) {
	switch (String(command.name)) {
		case 'ruby':
			ruby(command)
			break
		case 'gem':
			gem(command)
			break
	}
}

function ruby(command, preArguments, postArguments) {
	// The Ruby standard library is here (JRuby expects a "lib" subdirectory underneath)
	System.setProperty('jruby.home', command.sincerity.container.getLibrariesFile('ruby'))

	var mainArguments = [MAIN_CLASS]
	if (preArguments) {
		for (var i in preArguments) {
			mainArguments.push(preArguments[i])
		}
	}
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}
	if (postArguments) {
		for (var i in postArguments) {
			mainArguments.push(postArguments[i])
		}
	}
	command.sincerity.run('delegate:main', mainArguments)
}

function gem(command) {
	if ((command.arguments.length) > 0 && (command.arguments[0] == 'install')) {
		ruby(command, ['-S', 'gem'], ['--bindir', sincerity.container.getExecutablesFile()])
	}
	else {
		ruby(command, ['-S', 'gem'])
	}
}

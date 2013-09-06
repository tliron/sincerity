
importClass(Packages.scala.tools.jline.TerminalFactory)

var MAIN_CLASS = 'scala.tools.nsc.MainGenericRunner'

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['scala']
}

function run(command) {
	switch (String(command.name)) {
		case 'scala':
			scala(command)
			break
	}
}

function scala(command) {
	var runArguments = ['delegate:main', MAIN_CLASS]
	
	// Scala uses its own classpath, so we'll just make sure to duplicate it from the container
	runArguments.push('-classpath', command.sincerity.container.dependencies.classpath)
	
	var arguments = command.arguments
	for (var i in arguments) {
		runArguments.push(arguments[i])
	}
	command.sincerity.run(runArguments)
	
	// The Scala REPL (which uses JLine) does not restore terminal echo, so we will do it manually
	// (See: http://scala-forum.org/read.php?5,411,411)
	TerminalFactory.get().echoEnabled = true
}

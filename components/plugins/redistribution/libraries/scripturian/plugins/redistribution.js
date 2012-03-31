
var MAIN_CLASS = 'com.izforge.izpack.compiler.Compiler'
	
importClass(
	java.lang.System,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException)

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['izpack']
}

function run(command) {
	switch (String(command.name)) {
		case 'izpack':
			izpack(command)
			break
	}
}

function izpack(command) {
	var mainArguments = [MAIN_CLASS]
	
	command.parse = true
	if (command.arguments.length < 1) {
		throw new BadArgumentsCommandException(command, 'application title')
	}
	
	var title = command.arguments[0]
	var version = command.arguments.length > 1 ? command.arguments[1] : '1.0'

	System.setProperty('distribution.title', title)
	System.setProperty('distribution.version', version)

	mainArguments.push(
		command.sincerity.container.getConfigurationFile('izpack', 'installer.xml'),
		'-b', command.sincerity.container.root,
		'-o', command.sincerity.container.getFile('installer.jar'),
		'-k', 'standard'
	)
	
	command.sincerity.run('delegate:main', mainArguments)
}

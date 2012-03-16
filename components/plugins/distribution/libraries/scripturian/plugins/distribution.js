
var MAIN_CLASS = 'com.izforge.izpack.compiler.Compiler'
	
importClass(java.lang.System)

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
	
	mainArguments.push(
		command.sincerity.container.getConfigurationFile('izpack', 'installer.xml'),
		'-b', command.sincerity.container.root,
		'-o', command.sincerity.container.getFile('installer.jar'),
		'-k', 'standard'
	)
	
	System.setProperty('distribution.title', 'My Application')
	System.setProperty('distribution.version', '1.0')
	System.setProperty('distribution.url', 'http://myorganization.org/')
	
	/*
	var arguments = command.arguments
	for (var i in arguments) {
		mainArguments.push(arguments[i])
	}*/
	
	command.sincerity.run('delegate:main', mainArguments)
}

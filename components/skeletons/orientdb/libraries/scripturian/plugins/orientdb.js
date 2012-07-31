
importClass(java.lang.System)

System.setProperty('ORIENTDB_HOME', sincerity.container.root)
System.setProperty('orientdb.config.file', sincerity.container.getConfigurationFile('orientdb', 'server.conf'))
System.setProperty('orientdb.www.path', sincerity.container.getFile('web'))

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['server', 'console', 'gremlin']
}

function run(command) {
	switch (String(command.name)) {
		case 'server':
			server(command)
			break
		case 'console':
			console(command)
			break
		case 'gremlin':
			gremlin(command)
			break
	}
}

function server(command) {
	command.sincerity.run('delegate:main', ['com.orientechnologies.orient.server.OServerMain'])
}

function console(command) {
	var arguments = ['com.orientechnologies.orient.graph.console.OGremlinConsole']
	for (var i in command.arguments) {
		arguments.push(command.arguments[i])
	}
	command.sincerity.run('delegate:main', arguments)
}

function gremlin(command) {
	var arguments = ['com.tinkerpop.gremlin.groovy.console.Console']
	for (var i in command.arguments) {
		arguments.push(command.arguments[i])
	}
	command.sincerity.run('delegate:main', arguments)
}

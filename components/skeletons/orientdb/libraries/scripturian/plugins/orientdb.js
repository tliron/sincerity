
document.executeOnce('/sincerity/jvm/')

importClass(java.lang.System)

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
	try {
		command.sincerity.run('logging:logging')
	} catch(x) {}
	properties(command)
	command.sincerity.run('delegate:main', ['com.orientechnologies.orient.server.OServerMain'])
}

function console(command) {
	var arguments = ['com.orientechnologies.orient.graph.console.OGremlinConsole']
	for (var i in command.arguments) {
		arguments.push(command.arguments[i])
	}
	properties(command)
	command.sincerity.run('delegate:main', arguments)
}

function gremlin(command) {
	var arguments = ['com.tinkerpop.gremlin.groovy.console.Console']
	for (var i in command.arguments) {
		arguments.push(command.arguments[i])
	}
	properties(command)
	command.sincerity.run('delegate:main', arguments)
}

function properties(command) {
	System.setProperty('ORIENTDB_HOME', sincerity.container.root)
	System.setProperty('orientdb.config.file', sincerity.container.getConfigurationFile('orientdb', 'server.conf'))
	System.setProperty('orientdb.www.path', sincerity.container.getFile('web'))
	
	var file = command.sincerity.container.getConfigurationFile('orientdb', 'properties.conf')
	if (file.exists()) {
		var properties = Sincerity.JVM.loadProperties(file)
		for (var e = properties.propertyNames(); e.hasMoreElements(); ) {
			var name = e.nextElement()
			System.setProperty(name, properties.get(name))
		}
	}
}

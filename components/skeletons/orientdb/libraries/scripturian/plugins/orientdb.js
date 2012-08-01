
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
			orientdb(command, 'com.orientechnologies.orient.server.OServerMain', true)
			break
		case 'console':
			orientdb(command, 'com.orientechnologies.orient.graph.console.OGremlinConsole')
			break
		case 'gremlin':
			orientdb(command, 'com.tinkerpop.gremlin.groovy.console.Console')
			break
	}
}

function orientdb(command, className, isServer) {
	var arguments = [className]
	
	for (var i in command.arguments) {
		arguments.push(command.arguments[i])
	}
	try {
		command.sincerity.run('logging:logging')
	} catch(x) {}
	
	System.setProperty('ORIENTDB_HOME', sincerity.container.root)
	
	if (isServer) {
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

	command.sincerity.run('delegate:main', arguments)
}

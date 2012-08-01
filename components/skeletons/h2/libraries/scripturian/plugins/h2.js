
function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['server', 'console', 'cluster']
}

function run(command) {
	switch (String(command.name)) {
		case 'server':
			server(command)
			break
		case 'console':
			console(command)
			break
		case 'cluster':
			cluster(command)
			break
	}
}

function server(command) {
	h2(command, 'org.h2.tools.Server', 'server')
}

function console(command) {
	h2(command, 'org.h2.tools.Console', 'console')
}

function cluster(command) {
	h2(command, 'org.h2.tools.CreateCluster', 'cluster')
}

function h2(command, className, confName) {
	var arguments = [className]

	var file = command.sincerity.container.getConfigurationFile('h2', confName + '.conf')
	if (file.exists()) {
		var reader = new java.io.BufferedReader(new java.io.FileReader(file))
		try {
			while (null !== (line = reader.readLine())) {
				if ((line.length() == 0) || line.startsWith('#')) {
					continue
				}
				arguments.push(line)
			}
		}
		finally {
			reader.close()
		}
	}

	arguments.push('-baseDir')
	arguments.push(command.sincerity.container.getFile('data'))

	for (var i in command.arguments) {
		arguments.push(command.arguments[i])
	}
	
	try {
		command.sincerity.run('logging:logging')
	} catch(x) {}
	
	command.sincerity.run('delegate:main', arguments)
}

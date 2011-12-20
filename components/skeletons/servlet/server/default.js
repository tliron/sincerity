
importClass(
	org.eclipse.jetty.server.Server,
	org.eclipse.jetty.server.handler.ContextHandlerCollection,
	java.io.File)

var here

function executeAll(dir) {
	if (dir.directory) {
		var files = dir.listFiles()
		for (var f in files) {
			here = files[f]
			var path = sincerity.container.getRelativePath(here)
			document.execute('/' + path)
		}
	}
}
	
var server = new Server()

// Assemble server
var serverDir = sincerity.container.getFile('server')
executeAll(new File(serverDir, 'connectors'))
server.handler = new ContextHandlerCollection()
executeAll(new File(serverDir, 'contexts'))

// Start server
server.start()
sincerity.out.println('Started server')
server.join()

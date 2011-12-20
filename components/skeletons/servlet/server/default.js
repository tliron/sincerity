
importClass(
	org.eclipse.jetty.server.Server,
	org.eclipse.jetty.server.handler.ContextHandlerCollection,
	org.eclipse.jetty.webapp.WebAppContext,
	org.eclipse.jetty.security.HashLoginService,
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

// JMX (if available)
try {
	importClass(
		org.eclipse.jetty.jmx.MBeanContainer,
		org.eclipse.jetty.util.log.Log,
		java.lang.management.ManagementFactory)

	var mBeanServer = ManagementFactory.platformMBeanServer
	var mBeanContainer = new MBeanContainer(mBeanServer)
	mBeanContainer.addBean(Log.log)
	server.addBean(mBeanContainer)
	server.container.addEventListener(mBeanContainer)
}
catch (x) {}

// Assemble server
var serverDir = sincerity.container.getFile('server')
executeAll(new File(serverDir, 'connectors'))
server.handler = new ContextHandlerCollection()
executeAll(new File(serverDir, 'contexts'))

// Add wars
var warsDir = new File(serverDir, 'wars')
if (warsDir.directory) {
	var cacheDir = sincerity.container.getCacheFile('jetty', 'wars')
	var files = warsDir.listFiles()
	for (var f in files) {
		var file = files[f]
		var name = file.name
		if (name.endsWith('.war')) {
			name = name.substring(0, name.length() - 4)
			var context = new WebAppContext(server.handler, file, '/' + name)
			cacheDir.mkdirs()
			context.tempDirectory = new File(cacheDir, name)
			context.securityHandler.loginService = new HashLoginService() 
		}
	}
}

// Start server
server.start()
sincerity.out.println('Started server')
server.join()

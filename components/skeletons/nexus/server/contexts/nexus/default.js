
importClass(
	org.eclipse.jetty.webapp.WebAppContext,
	org.eclipse.jetty.webapp.WebAppClassLoader,
	java.lang.System)

System.setProperty('bundleBasedir', sincerity.container.getFile('nexus'))

var context = new WebAppContext()
context.contextPath = '/'
context.resourceBase = Sincerity.Container.getFileFromHere('web')
context.tempDirectory = Sincerity.Container.getFileFromHere('work')

// Plexus needs all classes to be in the same classloader
context.classLoader = new WebAppClassLoader(context)
for (var i = sincerity.container.dependencies.getClasspaths(false).iterator(); i.hasNext(); ) {
	var jar = i.next()
	// Nexus annoyingly *requires* that SLF4J run over Logback, so we will make sure not
	// to load the log4j implementation
	if (jar.name != 'slf4j-log4j12.jar') {
		context.classLoader.addClassPath(jar)
	}
}

server.handler.addHandler(context)


importClass(
	org.eclipse.jetty.webapp.WebAppContext,
	org.eclipse.jetty.security.HashLoginService,
	java.io.File)

try {
	var webInfDir = new File(new File(here, 'web'), 'WEB-INF')
	sincerity.run('java:compile', [new File(webInfDir, 'src'), new File(webInfDir, 'classes')])

	// The context
	var context = new WebAppContext()
	context.contextPath = '/webapp'
	context.resourceBase = new File(here, 'web')
	context.tempDirectory = new File(here, 'work')
	server.handler.addHandler(context)
}
catch (x) {
	x.javaException.printStackTrace()
}

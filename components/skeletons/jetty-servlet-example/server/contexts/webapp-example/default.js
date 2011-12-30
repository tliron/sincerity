
importClass(
	org.eclipse.jetty.webapp.WebAppContext,
	org.eclipse.jetty.security.HashLoginService)

try {
	sincerity.run('java:compile', [Savory.Sincerity.getFileFromHere('web', 'WEB-INF', 'src'), Savory.Sincerity.getFileFromHere('web', 'WEB-INF', 'classes')])

	// The context
	var context = new WebAppContext()
	context.contextPath = '/webapp'
	context.resourceBase = Savory.Sincerity.getFileFromHere('web')
	context.tempDirectory = Savory.Sincerity.getFileFromHere('work')
	server.handler.addHandler(context)
}
catch (x) {
	x.javaException.printStackTrace()
}

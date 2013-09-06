
importClass(
	org.eclipse.jetty.webapp.WebAppContext,
	org.eclipse.jetty.security.HashLoginService)

sincerity.run(['java:compile', Sincerity.Container.getFileFromHere('web', 'WEB-INF', 'src'), Sincerity.Container.getFileFromHere('web', 'WEB-INF', 'classes')])

// The context
var context = new WebAppContext()
context.contextPath = '/webapp'
context.resourceBase = Sincerity.Container.getFileFromHere('web')
context.tempDirectory = Sincerity.Container.getFileFromHere('work')
server.handler.addHandler(context)


importClass(
	org.eclipse.jetty.webapp.WebAppContext,
	java.lang.System)

System.setProperty('plexus.nexus-work', Sincerity.Container.getFileFromHere('work'))

var context = new WebAppContext()
context.contextPath = '/'
context.resourceBase = Sincerity.Container.getFileFromHere('web')
context.tempDirectory = Sincerity.Container.getFileFromHere('work')
context.addServerClass('org.slf4j.') // Avoid conflicts with Sincerity's logging plugin
server.handler.addHandler(context)

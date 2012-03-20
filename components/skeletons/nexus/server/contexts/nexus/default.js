
importClass(
	org.eclipse.jetty.webapp.WebAppContext,
	java.lang.System)

System.setProperty('plexus.nexus-work', Sincerity.Container.getFileFromHere('work'))

var context = new WebAppContext()
context.contextPath = '/'
context.resourceBase = Sincerity.Container.getFileFromHere('web')
context.tempDirectory = Sincerity.Container.getFileFromHere('work')

// Avoid conflicts with Sincerity's logging plugin
context.addServerClass('org.slf4j.')

// Plexus needs direct access to the HTTP client jar
context.extraClasspath = sincerity.container.getLibrariesFile('jars', 'org.apache.commons', 'commons-httpclient', '3.1-SONATYPE', 'commons-httpclient.jar')

server.handler.addHandler(context)

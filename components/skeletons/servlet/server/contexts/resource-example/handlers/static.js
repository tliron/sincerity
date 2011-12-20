
importClass(
	org.eclipse.jetty.server.handler.ResourceHandler,
	java.io.File)

var resource = new ResourceHandler()
resource.resourceBase = new File(here.parentFile.parentFile, 'static')
resource.directoriesListed = true
resource.welcomeFiles = ['index.html']
context.handler.addHandler(resource)

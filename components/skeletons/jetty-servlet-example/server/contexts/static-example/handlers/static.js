
importClass(
	org.eclipse.jetty.server.handler.ResourceHandler)

var resource = new ResourceHandler()
resource.resourceBase = Savory.Sincerity.getFileFromHere('..', 'static')
resource.directoriesListed = true
resource.welcomeFiles = ['index.html']
context.handler.addHandler(resource)


importClass(
	org.eclipse.jetty.server.handler.ResourceHandler)

var resource = new ResourceHandler()
resource.resourceBase = Sincerity.Container.getFileFromHere('..', 'static')
resource.directoriesListed = true
resource.welcomeFiles = ['index.html']
context.handler.addHandler(resource)

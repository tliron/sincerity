
importClass(
	org.restlet.resource.Directory,
	org.restlet.routing.Template)

var staticDir = Sincerity.Container.getFileFromHere('..', 'static') 
	
var directory = new Directory(app.context, staticDir.toURI())
directory.negotiatingContent = true
directory.listingAllowed = true
app.inboundRoot.attach('/static/', directory)

// We want the root URI to be handled by the directory
app.inboundRoot.attach('/', directory, Template.MODE_EQUALS)

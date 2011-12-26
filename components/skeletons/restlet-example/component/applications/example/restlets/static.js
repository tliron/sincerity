
importClass(
	org.restlet.resource.Directory,
	org.restlet.routing.Template,
	java.io.File)

var staticDir = new File(Savory.Sincerity.here.parentFile.parentFile, 'static').absoluteFile
	
var directory = new Directory(app.context, staticDir.toURI())
directory.listingAllowed = true
app.inboundRoot.attach('static/', directory)

// We want the root URI to be handled by the directory
app.inboundRoot.attach('', directory, Template.MODE_EQUALS)

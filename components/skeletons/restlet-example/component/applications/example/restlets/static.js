
importClass(
	org.restlet.resource.Directory,
	org.restlet.routing.Template,
	java.io.File)

var directory = new Directory(app.context, new File(Savory.Sincerity.here.parentFile.parentFile, 'static').absoluteFile.toURI())
directory.listingAllowed = true
router.attach('static/', directory)

// We want the root URI to be handled by the directory
router.attach('', directory, Template.MODE_EQUALS)

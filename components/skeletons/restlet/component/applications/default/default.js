
importClass(
	org.restlet.Application,
	org.restlet.routing.Router,
	org.restlet.routing.Template,
	org.restlet.resource.Directory,
	java.io.File)

var app = new Application(component.context.createChildContext())
app.name = 'Sincerity'
app.description = 'The default REST application for the Sincerity container'
app.author = 'Three Crickets'
app.owner = 'Free Software'
	
//
// Hosts
//
	
component.defaultHost.attach('/', app)

//
// Inbound root
//

var router = new Router(app.context)
app.inboundRoot = router

//
// Directory (static web)
//

var directory = new Directory(app.context, new File(here, 'web').toURI())
directory.listingAllowed = true
router.attach('', directory, Template.MODE_EQUALS)
router.attach('static/', directory)

// The directory needs to access file URIs
component.clients.add(Protocol.FILE)

//
// Resources
//

try {
sincerity.run('java:compile', [new File(here, 'java')])
router.attach('resource/', sincerity.container.dependencies.classLoader.loadClass('rest.DefaultResource'))
} catch(x) { x.printStackTrace() }

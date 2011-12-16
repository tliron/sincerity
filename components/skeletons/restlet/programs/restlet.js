
importClass(
	org.restlet.Component,
	org.restlet.data.Protocol,
	org.restlet.Application,
	org.restlet.routing.Router,
	org.restlet.routing.Template,
	org.restlet.resource.Directory)

//
// Logging
//

try {
sincerity.run('logging:initialize')
} catch(x) {}

//
// Component
//
	
var component = new Component()

//
// Application
//

var app = new Application(component.context.createChildContext())
app.name = 'Sincerity'
app.description = 'The default REST application for the Sincerity container'
app.author = 'Three Crickets'
app.owner = 'Free Software'
component.defaultHost.attach('/', app)

// The application's inbound root
var router = new Router(app.context)
app.inboundRoot = router

//
// Directory (static web)
//

var directory = new Directory(app.context, sincerity.container.getFile('web').toURI().toString())
directory.listingAllowed = true
router.attach('', directory, Template.MODE_EQUALS)
router.attach('static/', directory)

// The directory needs to access file URIs
component.clients.add(Protocol.FILE)

//
// Resources
//

try {
sincerity.run('java:compile')
router.attach('resource/', sincerity.container.dependencies.classLoader.loadClass('rest.DefaultResource'))
} catch(x) { x.printStackTrace() }

//
// Start server
//

component.servers.add(Protocol.HTTP, 8080)
component.start()

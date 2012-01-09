
document.executeOnce('/sincerity/container/')

importClass(
	org.restlet.Application,
	org.restlet.routing.Router,
	org.restlet.routing.Redirector,
	org.restlet.routing.Template)
	
// The application
var app = new Application(component.context.createChildContext())
app.name = 'Restlet Example'
app.description = 'The example application for the Restlet skeleton'
app.author = 'Three Crickets'
app.owner = 'Sincerity'
	
var addTrailingSlashRedirector = new Redirector(app.context, '{ri}/', Redirector.MODE_CLIENT_PERMANENT)

// Attach to hosts
// (Note that we are enforcing a trailing slash)
component.defaultHost.attach('/restlet-example', addTrailingSlashRedirector).matchingMode = Template.MODE_EQUALS
component.defaultHost.attach('/restlet-example', app)

// For this example, we'll also attach to the root
component.defaultHost.attach('', app)

// Inbound root
app.inboundRoot = new Router(app.context)
app.inboundRoot.routingMode = Router.MODE_BEST_MATCH

// Restlets
Sincerity.Container.executeAll('restlets')


document.executeOnce('/savory/sincerity/')

importClass(
	org.restlet.Application,
	org.restlet.routing.Router,
	org.restlet.routing.Template)

var settings = {}
var routes = {}

Savory.Sincerity.executeAll('types')
Savory.Sincerity.executeAll('settings')
Savory.Sincerity.executeAll('routes')

// The application
var app = new Application(component.context.createChildContext())
app.name = settings.application.name
app.description = settings.application.description
app.author = settings.application.author
app.owner = settings.application.owner

function getHost(name) {
	if (name == 'default') {
		return component.defaultHost
	}
	for (var i = component.hosts.iterator(); i.hasNext(); ) {
		var host = i.next()
		if (name == host.name) {
			return host
		}
	}
	return null
}

// Attach to hosts
for (var name in settings.hosts) {
	getHost(name).attach(settings.hosts[name], app)
}

// Inbound root
var router = new Router(app.context)
app.inboundRoot = router

// Attach routes
for (var uri in routes) {
	var restlet = routes[uri].create(app.context)
	print('Attaching ' + uri + ' to ' + restlet + '\n')
	router.attach(uri, restlet)
}

// Restlets
Savory.Sincerity.executeAll('restlets')

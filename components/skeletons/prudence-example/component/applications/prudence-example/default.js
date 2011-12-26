
document.executeOnce('/savory/sincerity/')

importClass(
	org.restlet.Application,
	org.restlet.routing.Router,
	org.restlet.routing.Template)

var settings = {routes: {}}

Savory.Sincerity.executeAll('types')
Savory.Sincerity.executeAll('settings')
Savory.Sincerity.executeAll('routes')

// The application
var app = new Application(component.context.createChildContext())
Savory.Objects.merge(app, settings.application)

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

function cleanUri(uri) {
	if ((uri.length > 0) && (uri[0] == '/')) {
		uri = uri.substring(1)
	}
	return uri
}

// Attach to hosts
for (var name in settings.hosts) {
	getHost(name).attach(settings.hosts[name], app)
}

// Inbound root
var router = new Router(app.context)
app.inboundRoot = router

// Attach routes
for (var uri in settings.routes) {
	var restlet = settings.routes[uri].create(app.context)
	print('Attaching ' + uri + ' to ' + restlet + '\n')
	if (uri[0] == '=') {
		uri = uri.substring(1)
		router.attach(cleanUri(uri), restlet, Template.MODE_EQUALS)
	}
	else {
		router.attach(cleanUri(uri), restlet)
	}
}

// Restlets
Savory.Sincerity.executeAll('restlets')

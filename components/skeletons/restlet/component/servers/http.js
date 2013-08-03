
//
// Adds an HTTP server.
//
// The default port is 8080, but it can also be set using the RESTLET_PORT environment
// variable or the restlet.port JVM property.
//

document.executeOnce('/sincerity/templates/')

importClass(
	org.restlet.Server,
	org.restlet.data.Protocol)

// Allow setting of port
var port = System.getProperty('restlet.port')
if (!Sincerity.Objects.exists(port)) {
	port = System.getenv('RESTLET_PORT')
}
if (Sincerity.Objects.exists(port)) {
	port = parseInt(port)
	if (isNaN(port)) {
		port = null
	}
}

// Default port
if (!Sincerity.Objects.exists(port)) {
	port = 8080
}

var server = new Server(Protocol.HTTP, port)
server.name = 'default'
component.servers.add(server)

// Add support for the X-FORWARDED-FOR header used by proxies, such as Apache's
// mod_proxy. This guarantees that request.clientInfo.upstreamAddress returns
// the upstream address behind the proxy.
server.context.parameters.add('useForwardedForHeader', 'true')

if (sincerity.verbosity >= 1) {
	println('Server: "{0}" {1} on port {2}'.cast(server.name, server.protocols.get(0), server.port))
}

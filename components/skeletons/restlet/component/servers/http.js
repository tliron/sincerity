
importClass(
	org.restlet.Server,
	org.restlet.data.Protocol)

var server = new Server(Protocol.HTTP, 8080)
server.name = 'default'
component.servers.add(server)

// Add support for the X-FORWARDED-FOR header used by proxies, such as Apache's
// mod_proxy. This guarantees that request.clientInfo.upstreamAddress returns
// the upstream address behind the proxy.
server.context.parameters.add('useForwardedForHeader', 'true')

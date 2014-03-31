
importClass(
	org.eclipse.jetty.server.Server,
	org.eclipse.jetty.server.ServerConnector,
	org.eclipse.jetty.server.HttpConfiguration,
	org.eclipse.jetty.server.HttpConnectionFactory,
	org.eclipse.jetty.server.SslConnectionFactory,
	org.eclipse.jetty.util.ssl.SslContextFactory,
	org.eclipse.jetty.server.SecureRequestCustomizer,
	org.eclipse.jetty.server.handler.HandlerList,
	org.eclipse.jetty.server.handler.ResourceHandler)

var port = 8080
var root = 'web'

var sslEnabled = false
var sslContextFactory = new SslContextFactory()

var spdyEnabled = false
var spdyVersion = 3

document.execute('/configuration/jetty/')

//
// Logging
//

try {
sincerity.run('logging:logging')
} catch(x) {}

//
// Server
//

var server = new Server()

var config = new HttpConfiguration()
var connector

if (sslEnabled) {
	config.addCustomizer(new SecureRequestCustomizer())
	var http = new HttpConnectionFactory(config)
	if (spdyEnabled) {
		importClass(
			org.eclipse.jetty.spdy.server.NPNServerConnectionFactory,
			org.eclipse.jetty.spdy.server.SPDYServerConnectionFactory,
			org.eclipse.jetty.spdy.server.http.HTTPSPDYServerConnectionFactory,
			org.eclipse.jetty.spdy.server.http.ReferrerPushStrategy)

		SPDYServerConnectionFactory.checkNPNAvailable()
		var push = new ReferrerPushStrategy()
		var spdy3 = new HTTPSPDYServerConnectionFactory(3, config, push)
		var spdy2 = new HTTPSPDYServerConnectionFactory(2, config, push)
		var npn
		if (spdyVersion == 3) {
			npn = new NPNServerConnectionFactory(spdy3.protocol, spdy2.protocol, http.protocol)
		}
		else { // if (spdyVersion == 2)
			npn = new NPNServerConnectionFactory(spdy2.protocol, http.protocol)
		}
		npn.defaultProtocol = http.protocol
		var ssl = new SslConnectionFactory(sslContextFactory, npn.protocol)
		if (spdyVersion == 3) {
			connector = new ServerConnector(server, [ssl, npn, spdy3, spdy2, http])
		}
		else { // if (spdyVersion == 2)
			connector = new ServerConnector(server, [ssl, npn, spdy2, http])
		}
	}
	else {
		config.addCustomizer(new SecureRequestCustomizer())
		var ssl = new SslConnectionFactory(sslContextFactory, http.protocol)
		connector = new ServerConnector(server, [ssl, http])
	}
}
else {
	var http = new HttpConnectionFactory(config)
	connector = new ServerConnector(server, [http])
}

connector.port = port
server.addConnector(connector)

// The handlers
var handlers = new HandlerList()
server.handler = handlers

//
// Resources (static web)
//

var resource = new ResourceHandler()
resource.resourceBase = sincerity.container.getFile(root)
resource.directoriesListed = true
handlers.addHandler(resource)

//
// Start server
//

server.start()
sincerity.out.println('Started web server on port ' + port)
server.join()

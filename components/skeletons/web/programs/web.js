
importClass(
	org.eclipse.jetty.server.Server,
	org.eclipse.jetty.server.handler.HandlerList,
	org.eclipse.jetty.server.handler.ResourceHandler)

//
// Logging
//

try {
sincerity.run('logging:initialize')
} catch(x) {}

//
// Server
//

var server = new Server(8080)

// The handlers
var handlers = new HandlerList()
server.handler = handlers

//
// Resources (static web)
//

var resource = new ResourceHandler()
resource.resourceBase = sincerity.container.getFile('web')
resource.directoriesListed = true
handlers.addHandler(resource)

//
// Servlets
//

try {
importClass(org.eclipse.jetty.servlet.ServletContextHandler)
var servlet = new ServletContextHandler()
servlet.contextPath = '/servlet/'

holder = servlet.addServlet(org.eclipse.jetty.servlet.DefaultServlet, '/tmp/*')
holder.setInitParameter('resourceBase', '/tmp')
holder.setInitParameter('pathInfoOnly', 'true')
handlers.addHandler(servlet)
} catch(x) {}

//
// Start server
//

server.start()
server.join()

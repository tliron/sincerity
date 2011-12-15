
importClass(
	org.eclipse.jetty.server.Server,
	org.eclipse.jetty.server.handler.HandlerList,
	org.eclipse.jetty.server.handler.ResourceHandler)
	//org.eclipse.jetty.servlet.ServletContextHandler)

var resource = new ResourceHandler()
resource.resourceBase = 'web'
resource.directoriesListed = true

/*var servlet = new ServletContextHandler()
servlet.contextPath = '/servlet/'

holder = servlet.addServlet(org.eclipse.jetty.servlet.DefaultServlet, '/tmp/*')
holder.setInitParameter('resourceBase', '/tmp')
holder.setInitParameter('pathInfoOnly', 'true')*/

var handlers = new HandlerList()
handlers.addHandler(resource)
//handlers.add(servlet)

var server = new Server(8080)
server.handler = handlers
server.start()
server.join()

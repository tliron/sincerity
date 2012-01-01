
importClass(
	org.eclipse.jetty.servlet.ServletContextHandler)

// The context
var context = new ServletContextHandler(server.handler, '/servlet')
context.displayName = 'The servlet example for the servlet skeleton'
context.resourceBase = Sincerity.Container.getFileFromHere('static') // used by DefaultServlet

// Assemble context
Sincerity.Container.include('servlets')

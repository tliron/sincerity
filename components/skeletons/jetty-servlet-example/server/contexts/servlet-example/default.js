
importClass(
	org.eclipse.jetty.servlet.ServletContextHandler,
	java.io.File)

// The context
var context = new ServletContextHandler(server.handler, '/servlet')
context.displayName = 'The servlet example for the servlet skeleton'
context.resourceBase = new File(Savory.Sincerity.here, 'static') // used by DefaultServlet

// Assemble context
Savory.Sincerity.include('servlets')

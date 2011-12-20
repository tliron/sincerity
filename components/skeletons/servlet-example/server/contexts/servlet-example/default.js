
importClass(
	org.eclipse.jetty.servlet.ServletContextHandler,
	java.io.File)

// The context
var context = new ServletContextHandler(server.handler, '/servlet')
context.displayName = 'The servlet example for the servlet skeleton'
context.resourceBase = new File(here, 'static') // used by DefaultServlet

// Assemble context
executeAll(new File(here, 'servlets'))

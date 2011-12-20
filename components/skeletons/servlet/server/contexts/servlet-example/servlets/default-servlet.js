
importClass(org.eclipse.jetty.servlet.DefaultServlet)

var servlet = new DefaultServlet()
context.addServlet(new ServletHolder(servlet), '/*')

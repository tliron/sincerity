
importClass(
	org.eclipse.jetty.servlet.ServletHolder)

try {
	sincerity.run('java:compile', [Savory.Sincerity.getFileFromHere('..', 'java')])

	var servlet = Savory.Sincerity.getClass('example.ExampleServlet').newInstance()
	context.addServlet(new ServletHolder(servlet), '/example/*')
}
catch (x) {
	x.javaException.printStackTrace()
}

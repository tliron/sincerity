
importClass(
	org.eclipse.jetty.servlet.ServletHolder)

try {
	sincerity.run('java:compile', [Sincerity.Container.getFileFromHere('..', 'java')])

	var servlet = Sincerity.Container.getClass('example.ExampleServlet').newInstance()
	context.addServlet(new ServletHolder(servlet), '/example/*')
}
catch (x) {
	x.javaException.printStackTrace()
}

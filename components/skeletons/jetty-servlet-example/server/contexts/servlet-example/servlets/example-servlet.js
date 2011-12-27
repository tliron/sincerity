
importClass(
	org.eclipse.jetty.servlet.ServletHolder,
	java.io.File)

try {
	sincerity.run('java:compile', [new File(Savory.Sincerity.here.parentFile.parentFile, 'java')])

	var servlet = Savory.Sincerity.getClass('example.ExampleServlet').newInstance()
	context.addServlet(new ServletHolder(servlet), '/example/*')
}
catch (x) {
	x.javaException.printStackTrace()
}

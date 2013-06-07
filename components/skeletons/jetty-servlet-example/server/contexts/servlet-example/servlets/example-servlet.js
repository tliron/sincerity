
document.executeOnce('/sincerity/jvm/')

importClass(
	org.eclipse.jetty.servlet.ServletHolder)

sincerity.run('java:compile', [Sincerity.Container.getFileFromHere('..', 'java')])

var theClass = Sincerity.JVM.getClass('example.ExampleServlet')
if (Sincerity.Objects.exists(theClass)) {
	context.addServlet(new ServletHolder(theClass.newInstance()), '/example/*')
}

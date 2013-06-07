
document.executeOnce('/sincerity/jvm/')

sincerity.run('java:compile', [Sincerity.Container.getFileFromHere('..', 'java')])

var theClass = Sincerity.JVM.getClass('example.ExampleResource')
if (Sincerity.Objects.exists(theClass)) {
	app.inboundRoot.attach('/resource/', theClass)
}

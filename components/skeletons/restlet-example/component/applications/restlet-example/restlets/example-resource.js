
document.executeOnce('/sincerity/jvm/')

var javaSourceDir = Sincerity.Container.getFileFromHere('..', 'java')

try {
	sincerity.run('java:compile', [javaSourceDir])
	app.inboundRoot.attach('/resource/', Sincerity.JVM.getClass('example.ExampleResource'))
}
catch (x) {
	x.javaException.printStackTrace()
}

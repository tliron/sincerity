
importClass(java.io.File)

var javaSourceDir = new File(Savory.Sincerity.here.parentFile.parentFile, 'java')
var classLoader = sincerity.container.dependencies.classLoader

try {
	sincerity.run('java:compile', [javaSourceDir])
	app.inboundRoot.attach('resource/', classLoader.loadClass('example.ExampleResource'))
}
catch(x) {
	x.javaException.printStackTrace()
}

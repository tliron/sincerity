
importClass(java.io.File)

var javaSourceDir = new File(Savory.Sincerity.here.parentFile.parentFile, 'java')

try {
	sincerity.run('java:compile', [javaSourceDir])
	app.inboundRoot.attach('resource/', Savory.Sincerity.getClass('example.ExampleResource'))
}
catch(x) {
	x.javaException.printStackTrace()
}

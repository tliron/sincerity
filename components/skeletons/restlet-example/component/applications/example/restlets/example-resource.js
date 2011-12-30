
var javaSourceDir = Savory.Sincerity.getFileFromHere('..', 'java')
print(javaSourceDir+'\n')

try {
	sincerity.run('java:compile', [javaSourceDir])
	app.inboundRoot.attach('resource/', Savory.Sincerity.getClass('example.ExampleResource'))
}
catch (x) {
	x.javaException.printStackTrace()
}

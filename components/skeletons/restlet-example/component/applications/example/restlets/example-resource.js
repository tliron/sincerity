
var javaSourceDir = Sincerity.Container.getFileFromHere('..', 'java')
print(javaSourceDir+'\n')

try {
	sincerity.run('java:compile', [javaSourceDir])
	app.inboundRoot.attach('resource/', Sincerity.Container.getClass('example.ExampleResource'))
}
catch (x) {
	x.javaException.printStackTrace()
}

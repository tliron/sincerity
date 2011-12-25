
importClass(java.io.File)

try {
	sincerity.run('java:compile', [new File(Savory.Sincerity.here.parentFile.parentFile, 'java')])
	router.attach('resource/', sincerity.container.dependencies.classLoader.loadClass('example.ExampleResource'))
}
catch(x) {
	x.javaException.printStackTrace()
}

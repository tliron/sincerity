
importClass(java.io.File)

try {
	sincerity.run('java:compile', [new File(here.parentFile.parentFile, 'java')])
	router.attach('resource/', sincerity.container.dependencies.classLoader.loadClass('example.ExampleResource'))
}
catch(x) {
	x.javaException.printStackTrace()
}

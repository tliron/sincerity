
function makeExecutable(file) {
	if (file.exists()) {
		if (undefined !== file.executable) { // JVM6+ only
			file.executable = true
		}
		else {
			sincerity.run('delegate:execute', 'chmod', '+x', file)
		}
	}
}

var nativeDir = sincerity.container.getLibrariesFile('native')
if (nativeDir.directory) {
	var files = nativeDir.listFiles()
	for (var f in files) {
		var file = files[f]
		if (file.name.startsWith('wrapper-')) {
			makeExecutable(file)
		}
	}
}

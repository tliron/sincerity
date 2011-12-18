
function makeExecutable(file) {
	if (file.exists()) {
		if (undefined !== file.executable) { // JVM6+ only
			file.executable = true
		}
		else {
			sincerity.run('delegate:launch', 'chmod', '+x', file)
		}
	}
}

makeExecutable(sincerity.container.getLibrariesFile('native', 'wrapper-linux-x86-64'))

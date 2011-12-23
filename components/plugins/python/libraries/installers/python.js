
function makeExecutable(file) {
	if (file.exists()) {
		if (undefined !== file.executable) { // JVM6+ only
			file.executable = true
		}
		else {
			sincerity.run('delegate:execute', ['chmod', '+x', file])
		}
	}
}

makeExecutable(sincerity.container.getExecutablesFile('python'))
makeExecutable(sincerity.container.getExecutablesFile('easy_install'))

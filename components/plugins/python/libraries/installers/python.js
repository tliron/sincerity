
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

var file = sincerity.container.getProgramsFile('python')
if (file.exists()) {
	makeExecutable(file)
}


function makeExecutable(file) {
	if (file.exists()) {
		if (undefined !== file.executable) { // JVM6+ only
			file.executable = true
		}
		else {
			sincerity.run('delegate:execute', ['--block', 'chmod', '+x', file])
		}
	}
}

makeExecutable(sincerity.container.getExecutablesFile('ruby'))
makeExecutable(sincerity.container.getExecutablesFile('jruby'))

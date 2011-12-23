
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

makeExecutable(sincerity.container.getExecutablesFile('ast'))
makeExecutable(sincerity.container.getExecutablesFile('gem'))
makeExecutable(sincerity.container.getExecutablesFile('irb'))
makeExecutable(sincerity.container.getExecutablesFile('jruby'))
makeExecutable(sincerity.container.getExecutablesFile('jrubyc'))
makeExecutable(sincerity.container.getExecutablesFile('rake'))
makeExecutable(sincerity.container.getExecutablesFile('rdoc'))
makeExecutable(sincerity.container.getExecutablesFile('ri'))
makeExecutable(sincerity.container.getExecutablesFile('ruby'))
makeExecutable(sincerity.container.getExecutablesFile('testrb'))

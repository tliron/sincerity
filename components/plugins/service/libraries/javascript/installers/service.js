
var binary = sincerity.container.getLibrariesFile('native', 'wrapper-linux-x86-64')
if (undefined !== binary.executable) {
	// JVM6+
	binary.executable = true
}
else {
	sincerity.exec(['chmod', '+x', binary])
}


importClass(
	org.restlet.Component,
	java.io.File)

var here
	
function executeAll(dir) {
	if (dir.directory) {
		var files = dir.listFiles()
		for (var f in files) {
			here = files[f]
			var path = sincerity.container.getRelativePath(here)
			document.execute('/' + path)
		}
	}
}

// The component
var component = new Component()

// Assemble the component
var componentDir = sincerity.container.getFile('component')
executeAll(new File(componentDir, 'clients'))
executeAll(new File(componentDir, 'servers'))
executeAll(new File(componentDir, 'hosts'))
executeAll(new File(componentDir, 'applications'))

// Start it!
component.start()

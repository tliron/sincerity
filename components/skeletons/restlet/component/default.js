
importClass(
	org.restlet.Component,
	java.io.File)

var here
	
function executeAll(file) {
	if (!(file instanceof File)) {
		file = new File(here, file)
	}
	if (file.directory) {
		var files = file.listFiles()
		for (var f in files) {
			var oldHere = here
			here = files[f]
			document.execute('/' + sincerity.container.getRelativePath(here))
			here = oldHere
		}
	}
	else {
		var name = file.name.split('\\.', 2)[0]
		var files = file.parentFile.listFiles()
		for (var f in files) {
			var oldHere = here
			here = files[f]
			var hereName = here.name.split('\\.', 2)[0]
			if (name == hereName) {
				document.execute('/' + sincerity.container.getRelativePath(here))
			}
			here = oldHere
		}
	}
}

// The component
var component = new Component()

// Assemble the component
here = sincerity.container.getFile('component')
executeAll('clients')
executeAll('servers')
executeAll('hosts')
executeAll('applications')

// Start it!
component.start()

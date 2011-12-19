
importClass(
	org.restlet.Component)

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
	
var component = new Component()

executeAll(sincerity.container.getFile('component', 'clients'))
executeAll(sincerity.container.getFile('component', 'servers'))
executeAll(sincerity.container.getFile('component', 'hosts'))
executeAll(sincerity.container.getFile('component', 'applications'))

component.start()


document.executeOnce('/sincerity/container/')

importClass(
	org.restlet.Component,
	java.io.File)
	
var initializers = []

// The component
var component = new Component()

// Assemble the component
Sincerity.Container.here = sincerity.container.getFile('component')
Sincerity.Container.executeAll('services')
Sincerity.Container.executeAll('clients')
Sincerity.Container.executeAll('servers')
Sincerity.Container.executeAll('hosts')
Sincerity.Container.executeAll('applications')

// Start it!
component.start()

// Call initializers
for (var i in initializers) {
	initializers[i]()
}

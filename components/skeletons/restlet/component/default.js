
document.executeOnce('/savory/sincerity/')

importClass(
	org.restlet.Component,
	java.io.File)
	
// The component
var component = new Component()

// Assemble the component
Savory.Sincerity.here = sincerity.container.getFile('component')
Savory.Sincerity.executeAll('clients')
Savory.Sincerity.executeAll('servers')
Savory.Sincerity.executeAll('hosts')
Savory.Sincerity.executeAll('applications')

// Start it!
component.start()

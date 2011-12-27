
document.executeOnce('/savory/sincerity/')

importClass(
	org.restlet.Component,
	java.io.File)
	
// The component
var component = new Component()

// Assemble the component
Savory.Sincerity.here = sincerity.container.getFile('component')
Savory.Sincerity.include('clients')
Savory.Sincerity.include('servers')
Savory.Sincerity.include('hosts')
Savory.Sincerity.include('applications')

// Start it!
component.start()

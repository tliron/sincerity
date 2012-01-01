
document.executeOnce('/sincerity/container/')

importClass(
	org.restlet.Component,
	java.io.File)
	
// The component
var component = new Component()

// Assemble the component
Sincerity.Container.here = sincerity.container.getFile('component')
Sincerity.Container.include('clients')
Sincerity.Container.include('servers')
Sincerity.Container.include('hosts')
Sincerity.Container.include('applications')

// Start it!
component.start()

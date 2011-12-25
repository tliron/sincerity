
document.executeOnce('/sincerity/sincerity/')

importClass(
	org.restlet.Component,
	java.io.File)

var here
	
// The component
var component = new Component()

// Assemble the component
Sincerity.Sincerity.here = sincerity.container.getFile('component')
Sincerity.Sincerity.executeAll('clients')
Sincerity.Sincerity.executeAll('servers')
Sincerity.Sincerity.executeAll('hosts')
Sincerity.Sincerity.executeAll('applications')

// Start it!
component.start()

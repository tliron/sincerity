
//
// Adds a file-based client.
//

importClass(
	org.restlet.data.Protocol)

// The directory needs to access file URIs
component.clients.add(Protocol.FILE)

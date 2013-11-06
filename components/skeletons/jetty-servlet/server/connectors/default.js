
// Jetty 9

importClass(
	org.eclipse.jetty.server.ServerConnector,
	org.eclipse.jetty.server.HttpConnectionFactory)

var connector = new ServerConnector(server, new HttpConnectionFactory())
connector.port = 8080
server.addConnector(connector)

/*
// Jetty 8

importClass(org.eclipse.jetty.server.nio.SelectChannelConnector)

var connector = new SelectChannelConnector()
connector.port = 8080
server.addConnector(connector)
*/

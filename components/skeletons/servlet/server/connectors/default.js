
importClass(org.eclipse.jetty.server.nio.SelectChannelConnector)

var connector = new SelectChannelConnector()
connector.port = 8080
server.addConnector(connector)


sincerity.run('logging:logging')

importClass(
	org.apache.log4j.LogManager,
	org.apache.log4j.net.SocketNode,
	java.net.ServerSocket,
	java.lang.Thread)

var port = 4560
var repository = LogManager.loggerRepository

println('Starting log4j server on port ' + port)

var serverSocket = new ServerSocket(port)
try {
	while (true) {
		var socket = serverSocket.accept()
		var socketNode = new SocketNode(socket, repository)
		var thread = new Thread(socketNode, 'log4j Server')
		thread.start()
	}
}
finally {
	serverSocket.close()
}

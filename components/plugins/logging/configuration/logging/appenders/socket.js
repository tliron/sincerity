
//
// The socket appender is useful in conjunction with a Log4j server:
//
//   sincerity delegate:start log4j-server
//
// And also with the Ganymede plugin for Eclipse:
//
//   http://ganymede.sourceforge.net/
//

/*
configuration.socketAppender({
	name: 'raw:socket',
	host: 'localhost', // host
	port: '4560' // the default for Log4j server is 4560. The default for Ganymede is 4445.
})

var origin = java.net.InetAddress.localHost.hostAddress

var appender = configuration.rewriteAppender({
	name: 'socket',
	appenders: ['raw:socket'],
	policy: {
		properties: {
			origin: origin
		}
	}
})

// This is a high-priority root appender
configuration.rootAppenders.unshift(appender)
*/